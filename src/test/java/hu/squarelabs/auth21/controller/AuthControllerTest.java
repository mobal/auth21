package hu.squarelabs.auth21.controller;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return OK status when login request is received")
        void shouldReturnOkStatusOnLoginRequest() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return TokenResponse with access token")
        void shouldReturnTokenResponseWithAccessToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token", notNullValue()))
                    .andExpect(jsonPath("$.access_token", equalTo("dummy-access-token")));
        }

        @Test
        @DisplayName("should return TokenResponse with refresh token")
        void shouldReturnTokenResponseWithRefreshToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refresh_token", notNullValue()))
                    .andExpect(jsonPath("$.refresh_token", equalTo("dummy-refresh-token")));
        }

        @Test
        @DisplayName("should return TokenResponse with expiration time")
        void shouldReturnTokenResponseWithExpirationTime() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expires_in", notNullValue()))
                    .andExpect(jsonPath("$.expires_in", equalTo(3600)));
        }

        @Test
        @DisplayName("should return JSON content type")
        void shouldReturnJsonContentType() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh-token")
    class RefreshTokenEndpoint {

        @Test
        @DisplayName("should return OK status when refresh token request is received")
        void shouldReturnOkStatusOnRefreshTokenRequest() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return TokenResponse with new access token")
        void shouldReturnTokenResponseWithNewAccessToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token", equalTo("dummy-access-token")));
        }

        @Test
        @DisplayName("should return TokenResponse with new refresh token")
        void shouldReturnTokenResponseWithNewRefreshToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refresh_token", equalTo("dummy-refresh-token")));
        }

        @Test
        @DisplayName("should return JSON content type")
        void shouldReturnJsonContentType() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("should return OK status when register request is received")
        void shouldReturnOkStatusOnRegisterRequest() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return TokenResponse with access token")
        void shouldReturnTokenResponseWithAccessToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token", equalTo("dummy-access-token")));
        }

        @Test
        @DisplayName("should return TokenResponse with refresh token")
        void shouldReturnTokenResponseWithRefreshToken() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refresh_token", equalTo("dummy-refresh-token")));
        }

        @Test
        @DisplayName("should return TokenResponse with expiration time")
        void shouldReturnTokenResponseWithExpirationTime() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expires_in", equalTo(3600)));
        }

        @Test
        @DisplayName("should return JSON content type")
        void shouldReturnJsonContentType() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}
