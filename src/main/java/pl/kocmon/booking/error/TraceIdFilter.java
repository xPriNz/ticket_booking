package pl.kocmon.booking.error;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Wstrzykuje traceId do {@link MDC} na czas obsługi requesta.
 * <p>
 * Dzięki temu każdy log wygenerowany podczas obsługi requesta ma to samo ID,
 * a {@link GlobalExceptionHandler} może je dołączyć do odpowiedzi błędu.
 * Gdy klient zgłosi problem z konkretnym traceId, znajdziesz jego request
 * w logach w kilka sekund.
 * <p>
 * Jeśli klient wysłał nagłówek {@code X-Trace-Id}, używamy jego wartości
 * (przydatne przy propagacji tracingu między serwisami). W przeciwnym razie
 * generujemy nowy UUID. Wartość zawsze wraca do klienta w tym samym nagłówku.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "traceId";
    public static final String HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        String traceId = Optional.ofNullable(request.getHeader(HEADER))
            .filter(s -> !s.isBlank())
            .orElseGet(() -> UUID.randomUUID().toString());

        try {
            MDC.put(MDC_KEY, traceId);
            response.setHeader(HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            // ZAWSZE czyścić MDC — wątki są reużywane w puli
            MDC.remove(MDC_KEY);
        }
    }
}
