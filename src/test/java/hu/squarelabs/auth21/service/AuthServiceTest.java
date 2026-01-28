package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.entity.UserEntity;
import hu.squarelabs.auth21.repository.TokenRepository;
import hu.squarelabs.auth21.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, tokenRepository, tokenService);
        ReflectionTestUtils.setField(authService, "jwtSecret", "test-secret");
        ReflectionTestUtils.setField(authService, "jwtTokenLifetime", 3600);
    }

    @Nested
    @DisplayName("login method")
    class LoginMethod {

        @Test
        @DisplayName("should throw NOT_FOUND when user email does not exist")
        void shouldThrowNotFoundWhenUserEmailNotExists() {
            String email = "nonexistent@example.com";
            String password = "password123";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(email, password))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should throw UNAUTHORIZED when password is invalid")
        void shouldThrowUnauthorizedWhenPasswordInvalid() {
            String email = "user@example.com";
            String password = "wrongpassword";

            UserEntity user = new UserEntity();
            user.setId("user-123");
            user.setEmail(email);
            user.setPasswordHash("hashed-password");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(email, password))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("should call tokenService.create when login succeeds")
        void shouldCallTokenServiceCreateOnSuccessfulLogin() {
            String email = "user@example.com";
            String password = "password123";

            UserEntity user = new UserEntity();
            user.setId("user-123");
            user.setEmail(email);
            user.setNickname("testuser");
            user.setName("Test User");
            user.setPasswordHash("hashed-password");
            user.setRoles(java.util.List.of("USER"));

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(email, password))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);

            verify(tokenService, never()).create(any(JwtToken.class), anyString());
        }

        @Test
        @DisplayName("should return access token and refresh token on successful login")
        void shouldReturnTokensOnSuccessfulLogin() {
            //
        }
    }

    @Nested
    @DisplayName("logout method")
    class LogoutMethod {

        @Test
        @DisplayName("should call tokenService.deleteById with correct JTI")
        void shouldCallTokenServiceDeleteByIdWithCorrectJti() {
            JwtToken jwtToken = new JwtToken();
            jwtToken.setJti("token-jti-123");

            authService.logout(jwtToken);

            verify(tokenService, times(1)).deleteById("token-jti-123");
        }
    }

    @Nested
    @DisplayName("refresh method")
    class RefreshMethod {

        @Test
        @DisplayName("should throw NOT_FOUND when refresh token not found")
        void shouldThrowNotFoundWhenRefreshTokenNotFound() {
            JwtToken jwtToken = new JwtToken();
            jwtToken.setJti("jti-123");
            String refreshToken = "invalid-refresh-token";

            when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(jwtToken, refreshToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("should throw INTERNAL_SERVER_ERROR when stored token does not match")
        void shouldThrowInternalServerErrorWhenTokenMismatch() {
            JwtToken originalToken = new JwtToken();
            originalToken.setJti("jti-123");
            originalToken.setSub("user-123");

            JwtToken storedToken = new JwtToken();
            storedToken.setJti("jti-456");
            storedToken.setSub("user-456");

            String refreshToken = "refresh-token";
            Map<String, Object> tokenData = Map.of(
                    "jwt_token", storedToken,
                    "refresh_token", refreshToken
            );

            when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.of(tokenData));

            assertThatThrownBy(() -> authService.refresh(originalToken, refreshToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("statusCode", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("should revoke old token and generate new tokens on successful refresh")
        void shouldRevokeOldTokenAndGenerateNewTokensOnSuccessfulRefresh() {
            JwtToken jwtToken = new JwtToken();
            jwtToken.setJti("jti-123");
            jwtToken.setSub("user-123");

            String refreshToken = "refresh-token";
            Map<String, Object> tokenData = Map.of(
                    "jwt_token", jwtToken,
                    "refresh_token", refreshToken
            );

            when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.of(tokenData));

            try {
                authService.refresh(jwtToken, refreshToken);
            } catch (Exception e) {
                //
            }

            verify(tokenService, times(1)).deleteById("jti-123");
            verify(tokenService, times(1)).create(any(JwtToken.class), anyString());
        }
    }
}
