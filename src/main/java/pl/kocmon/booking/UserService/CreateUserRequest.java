package pl.kocmon.booking.UserService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload dla POST /users. Walidacja wykonuje się automatycznie
 * dzięki adnotacji @Valid w kontrolerze.
 */
public record CreateUserRequest(

    @NotBlank(message = "email is required")
    @Email(message = "must be a valid email")
    String email,

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    String password

) {}
