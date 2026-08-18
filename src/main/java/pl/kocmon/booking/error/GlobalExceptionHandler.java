package pl.kocmon.booking.error;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Globalny handler wyjątków dla całego API.
 * <p>
 * Odpowiedzi używają formatu <b>RFC 7807 (Problem Details)</b> —
 * {@code application/problem+json}. Do standardowych pól dokładamy własne:
 * {@code code} (identyfikator błędu dla klienta), {@code traceId} (do korelacji z logami)
 * oraz {@code timestamp}.
 * <p>
 * Rozszerzenie {@link ResponseEntityExceptionHandler} daje nam wbudowaną obsługę
 * standardowych wyjątków Spring MVC — nadpisujemy tylko te, którym chcemy dodać
 * własne pola.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI ERROR_BASE_URI = URI.create("https://api.booking.kocmon.pl/errors/");

    // === Własne wyjątki aplikacyjne ===

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex, HttpServletRequest request) {
        HttpStatus status = ex.getCode().getStatus();
        logByStatus(status, ex, request);

        return problem(status, ex.getMessage(), ex.getCode(), request.getRequestURI());
    }

    // === Baza danych ===

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(
        DataIntegrityViolationException ex, HttpServletRequest request
    ) {
        // WAŻNE: logujemy pełny wyjątek (zawiera SQL), ale klientowi nie zdradzamy szczegółów
        log.error("Data integrity violation at {}", request.getRequestURI(), ex);

        return problem(
            HttpStatus.CONFLICT,
            "The operation violates a data constraint",
            ErrorCode.CONFLICT,
            request.getRequestURI()
        );
    }

    // === Security ===

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
        AccessDeniedException ex, HttpServletRequest request
    ) {
        log.warn("Access denied at {} for user {}", request.getRequestURI(), currentUser());
        return problem(
            HttpStatus.FORBIDDEN,
            "You don't have permission to access this resource",
            ErrorCode.FORBIDDEN,
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(
        AuthenticationException ex, HttpServletRequest request
    ) {
        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return problem(
            HttpStatus.UNAUTHORIZED,
            "Authentication required",
            ErrorCode.UNAUTHORIZED,
            request.getRequestURI()
        );
    }

    // === Walidacja parametrów (@Validated na @PathVariable / @RequestParam) ===
    // Walidacja body (@Valid @RequestBody) rzuca MethodArgumentNotValidException
    // i jest obsłużona niżej w metodzie handleMethodArgumentNotValid (override).

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
        ConstraintViolationException ex, HttpServletRequest request
    ) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
            .map(v -> Map.of(
                "field", v.getPropertyPath().toString(),
                "message", v.getMessage()
            ))
            .toList();

        log.warn("Constraint violation at {}: {}", request.getRequestURI(), errors);

        ProblemDetail problem = problem(
            HttpStatus.BAD_REQUEST,
            "Invalid parameters",
            ErrorCode.VALIDATION_ERROR,
            request.getRequestURI()
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    // === Catch-all — ostatnia linia obrony ===

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        // NIE ujawniamy ex.getMessage() — może zawierać wrażliwe dane
        return problem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact support with the traceId.",
            ErrorCode.INTERNAL_ERROR,
            request.getRequestURI()
        );
    }

    // === Nadpisania z ResponseEntityExceptionHandler ===
    // Spring ma już handlery dla tych wyjątków, ale domyślnie nie dokładają
    // naszych pól (code, traceId, errors). Nadpisujemy, żeby format był spójny.

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of(
                "field", fe.getField(),
                "message", Objects.requireNonNullElse(fe.getDefaultMessage(), "invalid value")
            ))
            .toList();

        log.warn("Validation failed at {}: {}", pathOf(request), errors);

        ProblemDetail problem = problem(
            HttpStatus.BAD_REQUEST,
            "Request validation failed",
            ErrorCode.VALIDATION_ERROR,
            pathOf(request)
        );
        problem.setProperty("errors", errors);

        return ResponseEntity.status(status).headers(headers).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Malformed JSON at {}: {}", pathOf(request), ex.getMessage());

        return ResponseEntity.status(status).headers(headers).body(problem(
            HttpStatus.BAD_REQUEST,
            "Malformed JSON request",
            ErrorCode.MALFORMED_JSON,
            pathOf(request)
        ));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        org.springframework.beans.TypeMismatchException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        String detail;
        if (ex instanceof MethodArgumentTypeMismatchException matme) {
            detail = "Parameter '%s' has invalid value '%s'".formatted(
                matme.getName(), matme.getValue()
            );
        } else {
            detail = "Type mismatch: " + ex.getMessage();
        }

        log.warn("{} at {}", detail, pathOf(request));

        return ResponseEntity.status(status).headers(headers).body(problem(
            HttpStatus.BAD_REQUEST,
            detail,
            ErrorCode.BAD_REQUEST,
            pathOf(request)
        ));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Method not supported at {}: {}", pathOf(request), ex.getMethod());

        return ResponseEntity.status(status).headers(headers).body(problem(
            HttpStatus.METHOD_NOT_ALLOWED,
            "HTTP method " + ex.getMethod() + " is not supported for this endpoint",
            ErrorCode.METHOD_NOT_ALLOWED,
            pathOf(request)
        ));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        // Wymaga w application.properties: spring.mvc.throw-exception-if-no-handler-found=true
        return ResponseEntity.status(status).headers(headers).body(problem(
            HttpStatus.NOT_FOUND,
            "Endpoint not found",
            ErrorCode.ENDPOINT_NOT_FOUND,
            pathOf(request)
        ));
    }

    // === Helpers ===

    /**
     * Buduje ProblemDetail z naszymi standardowymi polami.
     */
    private ProblemDetail problem(HttpStatus status, String detail, ErrorCode code, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(ERROR_BASE_URI.resolve(code.name().toLowerCase().replace('_', '-')));
        problem.setTitle(humanize(code));
        problem.setInstance(URI.create(path));
        problem.setProperty("code", code.name());
        problem.setProperty("traceId", currentTraceId());
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    private void logByStatus(HttpStatus status, Exception ex, HttpServletRequest request) {
        if (status.is5xxServerError()) {
            log.error("Server error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        } else {
            // Client errors — mniej szczegółów, bez stack trace'a (to nie nasz bug)
            log.warn("Client error at {}: {}", request.getRequestURI(), ex.getMessage());
        }
    }

    private String humanize(ErrorCode code) {
        String name = code.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String pathOf(WebRequest request) {
        // WebRequest.getDescription zwraca "uri=/foo" — obcinamy prefix
        return request.getDescription(false).replaceFirst("^uri=", "");
    }

    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        // Fallback — na wypadek, gdyby filtr się nie odpalił (np. w testach unit)
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    private String currentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName)
            .orElse("anonymous");
    }
}
