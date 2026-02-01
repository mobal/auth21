package hu.squarelabs.auth21.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
  }

  @Nested
  @DisplayName("POST /api/v1/login")
  class LoginEndpoint {

    @Test
    @DisplayName("should return OK status when login request is received")
    void shouldReturnOkStatusOnLoginRequest() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return TokenResponse with access token")
    void shouldReturnTokenResponseWithAccessToken() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.access_token", notNullValue()))
          .andExpect(jsonPath("$.access_token", equalTo("dummy-access-token")));
    }

    @Test
    @DisplayName("should return TokenResponse with refresh token")
    void shouldReturnTokenResponseWithRefreshToken() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.refresh_token", notNullValue()))
          .andExpect(jsonPath("$.refresh_token", equalTo("dummy-refresh-token")));
    }

    @Test
    @DisplayName("should return TokenResponse with expiration time")
    void shouldReturnTokenResponseWithExpirationTime() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.expires_in", notNullValue()))
          .andExpect(jsonPath("$.expires_in", equalTo(3600)));
    }

    @Test
    @DisplayName("should return JSON content type")
    void shouldReturnJsonContentType() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/refresh")
  class RefreshTokenEndpoint {

    @Test
    @DisplayName("should return OK status when refresh token request is received")
    void shouldReturnOkStatusOnRefreshTokenRequest() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"dummy-refresh-token\"}"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return TokenResponse with new access token")
    void shouldReturnTokenResponseWithNewAccessToken() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"dummy-refresh-token\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.access_token", equalTo("dummy-access-token")));
    }

    @Test
    @DisplayName("should return TokenResponse with new refresh token")
    void shouldReturnTokenResponseWithNewRefreshToken() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"dummy-refresh-token\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.refresh_token", equalTo("dummy-refresh-token")));
    }

    @Test
    @DisplayName("should return JSON content type")
    void shouldReturnJsonContentType() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"refresh_token\":\"dummy-refresh-token\"}"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
  }
}
