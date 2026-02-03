package hu.squarelabs.auth21.controller;

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
  }
}
