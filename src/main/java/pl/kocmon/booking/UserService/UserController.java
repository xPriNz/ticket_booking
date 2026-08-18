package pl.kocmon.booking.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import pl.kocmon.booking.error.ConflictException;
import pl.kocmon.booking.error.NotFoundException;

/**
 * Minimalny kontroler demonstrujący integrację z GlobalExceptionHandler.
 * <p>
 * <b>@Validated</b> na klasie — potrzebne, żeby walidować @PathVariable / @RequestParam.
 * <br>
 * <b>@Valid</b> na @RequestBody — waliduje pola DTO wg adnotacji na rekordzie.
 * <p>
 * W realnej aplikacji cała logika trafiłaby do warstwy serwisowej —
 * tu upraszczam, żeby pokazać samą obsługę błędów.
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    // Prowizoryczny "storage" — normalnie tu byłby UserRepository
    private final Map<Long, String> users = new ConcurrentHashMap<>(Map.of(
        1L, "alice@example.com",
        2L, "bob@example.com"
    ));

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable @Positive Long id) {
        String email = users.get(id);
        if (email == null) {
            throw NotFoundException.user(id);
        }
        return Map.of("id", id, "email", email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@Valid @RequestBody CreateUserRequest request) {
        boolean emailTaken = users.values().stream()
            .anyMatch(existing -> existing.equalsIgnoreCase(request.email()));
        if (emailTaken) {
            throw ConflictException.emailAlreadyUsed(request.email());
        }
        long newId = users.size() + 1L;
        users.put(newId, request.email());
        return Map.of("id", newId, "email", request.email(), "name", request.name());
    }
}
