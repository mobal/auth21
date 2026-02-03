package hu.squarelabs.auth21.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import hu.squarelabs.auth21.model.dto.request.LoginRequest;
import hu.squarelabs.auth21.model.dto.request.RefreshRequest;
import hu.squarelabs.auth21.model.dto.request.RegistrationRequest;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("AuthController")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;

  @Mock private AuthService authService;

  private AuthController authController;

  @BeforeEach
  void setUp() throws Exception {
    authController = new AuthController(authService);
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

    lenient()
        .when(authService.login(anyString(), anyString()))
        .thenReturn(new TokenResponse("dummy-access-token", "dummy-refresh-token", 3600L));

    lenient()
        .when(authService.refresh(anyString()))
        .thenReturn(new TokenResponse("dummy-access-token", "dummy-refresh-token", 3600L));

    lenient().when(authService.register(any(RegistrationRequest.class))).thenReturn("user-123");

    lenient().doNothing().when(authService).logout(anyString());
  }

  @Nested
  @DisplayName("POST /api/v1/login")
  class Login {

    @Test
    @DisplayName("returns OK with TokenResponse containing access and refresh tokens")
    void returnsOkWithTokenResponseContainingAccessAndRefreshTokens() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.access_token").value("dummy-access-token"))
          .andExpect(jsonPath("$.refresh_token").value("dummy-refresh-token"))
          .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is missing")
    void returnsBadRequestWhenEmailIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"password\":\"password123\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is missing")
    void returnsBadRequestWhenPasswordIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is invalid")
    void returnsBadRequestWhenEmailIsInvalid() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"invalid-email\",\"password\":\"password123\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is blank")
    void returnsBadRequestWhenEmailIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"\",\"password\":\"password123\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is blank")
    void returnsBadRequestWhenPasswordIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("calls authService.login with provided credentials")
    void callsAuthServiceLoginWithProvidedCredentials() throws Exception {
      mockMvc.perform(
          post("/api/v1/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"));

      verify(authService).login("user@example.com", "password123");
    }
  }

  @Nested
  @DisplayName("POST /api/v1/refresh")
  class RefreshToken {

    @Test
    @DisplayName("returns OK with new TokenResponse containing access and refresh tokens")
    void returnsOkWithNewTokenResponseContainingAccessAndRefreshTokens() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"dummy-refresh-token\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.access_token").value("dummy-access-token"))
          .andExpect(jsonPath("$.refresh_token").value("dummy-refresh-token"));
    }

    @Test
    @DisplayName("calls authService.refresh with provided refresh token")
    void callsAuthServiceRefreshWithProvidedRefreshToken() throws Exception {
      mockMvc.perform(
          post("/api/v1/refresh")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"refresh_token\":\"my-refresh-token\"}"));

      verify(authService).refresh("my-refresh-token");
    }

    @Test
    @DisplayName("returns error when authService throws ResponseStatusException")
    void returnsErrorWhenAuthServiceThrowsResponseStatusException() throws Exception {
      when(authService.refresh(anyString()))
          .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"invalid-token\"}"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/register")
  class Register {

    @Test
    @DisplayName("returns CREATED with location header when registration is successful")
    void returnsCreatedWithLocationHeaderWhenRegistrationIsSuccessful() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\"}"))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/users/user-123"));
    }

    @Test
    @DisplayName("calls authService.register with provided registration request")
    void callsAuthServiceRegisterWithProvidedRegistrationRequest() throws Exception {
      mockMvc.perform(
          post("/api/v1/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(
                  "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\",\"displayName\":\"New User\"}"));

      verify(authService).register(any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is missing")
    void returnsBadRequestWhenEmailIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"password\":\"password123\",\"username\":\"newuser\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is missing")
    void returnsBadRequestWhenPasswordIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"newuser@example.com\",\"username\":\"newuser\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when username is missing")
    void returnsBadRequestWhenUsernameIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"newuser@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is invalid")
    void returnsBadRequestWhenEmailIsInvalid() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"invalid-email\",\"password\":\"password123\",\"username\":\"newuser\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when email is blank")
    void returnsBadRequestWhenEmailIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"\",\"password\":\"password123\",\"username\":\"newuser\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when password is blank")
    void returnsBadRequestWhenPasswordIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"newuser@example.com\",\"password\":\"\",\"username\":\"newuser\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns BAD_REQUEST when username is blank")
    void returnsBadRequestWhenUsernameIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("accepts registration when displayName is null")
    void acceptsRegistrationWhenDisplayNameIsNull() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\"}"))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/logout")
  class Logout {

    @Test
    @DisplayName("returns NO_CONTENT when logout is successful")
    void returnsNoContentWhenLogoutIsSuccessful() throws Exception {
      mockMvc
          .perform(get("/api/v1/logout").header("Authorization", "Bearer my-access-token"))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("calls authService.logout with token from Authorization header")
    void callsAuthServiceLogoutWithTokenFromAuthorizationHeader() throws Exception {
      mockMvc.perform(get("/api/v1/logout").header("Authorization", "Bearer my-access-token"));

      verify(authService).logout("my-access-token");
    }

    @Test
    @DisplayName("strips Bearer prefix from Authorization header")
    void stripsBearerPrefixFromAuthorizationHeader() throws Exception {
      mockMvc.perform(
          get("/api/v1/logout").header("Authorization", "Bearer token-with-bearer-prefix"));

      verify(authService).logout("token-with-bearer-prefix");
    }

    @Test
    @DisplayName("returns error when authService throws ResponseStatusException")
    void returnsErrorWhenAuthServiceThrowsResponseStatusException() throws Exception {
      doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
          .when(authService)
          .logout(anyString());

      mockMvc
          .perform(get("/api/v1/logout").header("Authorization", "Bearer invalid-token"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("has no PreAuthorize annotation")
    void hasNoPreAuthorizeAnnotation() throws Exception {
      var method = AuthController.class.getMethod("logout", String.class);
      var preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

      assert preAuthorizeAnnotation == null : "logout endpoint should not have @PreAuthorize";
    }
  }

  @Nested
  @DisplayName("Authorization Rules")
  class AuthorizationRules {

    @Nested
    @DisplayName("POST /api/v1/register")
    class RegisterAuthorizationRules {

      @Test
      @DisplayName("is protected by PreAuthorize annotation requiring ADMIN role")
      void isProtectedByPreAuthorizeAnnotationRequiringAdminRole() throws Exception {
        var method = AuthController.class.getMethod("register", RegistrationRequest.class);
        var preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

        assert preAuthorizeAnnotation != null : "register endpoint should have @PreAuthorize";
        assert preAuthorizeAnnotation.value().equals("hasRole('ADMIN')")
            : "register should require ADMIN role";
      }

      @Test
      @DisplayName("allows access when invoked with ADMIN role using SecurityContext")
      void allowsAccessWhenInvokedWithAdminRoleUsingSecurityContext() throws Exception {
        when(authService.register(any(RegistrationRequest.class))).thenReturn("user-123");

        mockMvc.perform(
            post("/api/v1/register")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"newuser@example.com\",\"password\":\"password123\",\"username\":\"newuser\"}"));

        verify(authService).register(any(RegistrationRequest.class));
      }
    }

    @Nested
    @DisplayName("POST /api/v1/login")
    class LoginAuthorizationRules {

      @Test
      @DisplayName("has no PreAuthorize annotation")
      void hasNoPreAuthorizeAnnotation() throws Exception {
        var method = AuthController.class.getMethod("login", LoginRequest.class);
        var preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

        assert preAuthorizeAnnotation == null : "login endpoint should not have @PreAuthorize";
      }
    }

    @Nested
    @DisplayName("POST /api/v1/refresh")
    class RefreshAuthorizationRules {

      @Test
      @DisplayName("has no PreAuthorize annotation")
      void hasNoPreAuthorizeAnnotation() throws Exception {
        var method = AuthController.class.getMethod("refresh", RefreshRequest.class);
        var preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

        assert preAuthorizeAnnotation == null : "refresh endpoint should not have @PreAuthorize";
      }
    }
  }
}
