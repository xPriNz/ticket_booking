package pl.kocmon.booking.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.junit.jupiter.api.BeforeEach;

/**
 * Testy integracyjne pokazujące, że GlobalExceptionHandler poprawnie mapuje
 * różne typy wyjątków na odpowiedzi w formacie RFC 7807 (Problem Details).
 * <p>
 * Używamy pełnego kontekstu Springa (@SpringBootTest), żeby złapać też
 * frameworkowe wyjątki jak MethodArgumentNotValidException.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
        "org.springframework.boot.autoconfigure.session.SessionAutoConfiguration," +
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration," +
        "org.springframework.modulith.events.jdbc.JdbcEventPublicationAutoConfiguration"
})
class GlobalExceptionHandlerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // === Własne wyjątki aplikacyjne ===

    @Test
    @WithMockUser
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.detail").value("User 999 not found"))
            .andExpect(jsonPath("$.instance").value("/api/users/999"))
            .andExpect(jsonPath("$.traceId").value(notNullValue()))
            .andExpect(jsonPath("$.timestamp").value(notNullValue()));
    }

    @Test
    @WithMockUser
    void shouldReturn409WhenEmailAlreadyUsed() throws Exception {
        String json = """
            {
              "email": "alice@example.com",
              "name": "Alice II",
              "password": "verySecret123"
            }
            """;

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"))
            .andExpect(jsonPath("$.detail").value(containsString("alice@example.com")));
    }

    // === Walidacja (@Valid @RequestBody) ===

    @Test
    @WithMockUser
    void shouldReturn400WithFieldErrorsForInvalidBody() throws Exception {
        String invalidJson = """
            {
              "email": "not-an-email",
              "name": "",
              "password": "short"
            }
            """;

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors").isArray())
            // Sprawdzamy per pole — bez zakładania kolejności
            .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @WithMockUser
    void shouldReturn400ForMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors.length()").value(3));
    }

    // === Walidacja parametrów (@Validated @PathVariable) ===

    @Test
    @WithMockUser
    void shouldReturn400ForNegativePathVariable() throws Exception {
        // @Positive na id — powinno rzucić ConstraintViolationException
        mockMvc.perform(get("/api/users/-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // === Frameworkowe wyjątki ===

    @Test
    @WithMockUser
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ this is not json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    @Test
    @WithMockUser
    void shouldReturn400ForInvalidPathVariableType() throws Exception {
        // /api/users/abc — abc nie da się zrzutować na Long
        mockMvc.perform(get("/api/users/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.detail").value(containsString("abc")));
    }

    @Test
    @WithMockUser
    void shouldReturn405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(patch("/api/users/1").with(csrf()))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @WithMockUser
    void shouldReturn404ForUnknownEndpoint() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"));
    }

    // === TraceId ===

    @Test
    @WithMockUser
    void shouldEchoTraceIdFromRequestHeader() throws Exception {
        String traceId = "custom-trace-id-12345";

        mockMvc.perform(get("/api/users/999").header("X-Trace-Id", traceId))
            .andExpect(status().isNotFound())
            .andExpect(header().string("X-Trace-Id", traceId))
            .andExpect(jsonPath("$.traceId").value(traceId));
    }

    @Test
    @WithMockUser
    void shouldGenerateTraceIdWhenNotProvided() throws Exception {
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.traceId").value(notNullValue()));
    }

    // === Happy path — sanity check ===

    @Test
    @WithMockUser
    void shouldReturnUserWhenExists() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    // === Bezpieczeństwo — brak wycieku szczegółów ===
    // Ten test pilnuje, żeby nikt niechcący nie zmienił handlera tak,
    // że zaczyna zwracać ex.getMessage() z catch-all.

    @Test
    @WithMockUser
    void catchAllShouldNotLeakInternalDetails() throws Exception {
        // Wywołanie /api/users/abc → MethodArgumentTypeMismatchException.
        // Nawet dla nietypowych błędów, klient nie powinien dostać nazwy
        // klasy wyjątku ani stack trace'a.
        mockMvc.perform(get("/api/users/abc"))
            .andExpect(jsonPath("$.detail").value(not(containsString("Exception"))))
            .andExpect(jsonPath("$.detail").value(not(containsString("java."))))
            .andExpect(jsonPath("$.detail").value(not(containsString("org.springframework"))));
    }
}
