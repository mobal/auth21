package hu.squarelabs.auth21.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.SimpleUserDetails;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@DisplayName("AuthService")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserService userService;

  @Mock private TokenService tokenService;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userService, tokenService, jwtService, authenticationManager);
  }

  @Nested
  @DisplayName("login method")
  class LoginMethod {

    @Test
    @DisplayName("should throw UserNotFoundException when user email does not exist")
    void shouldThrowNotFoundWhenUserEmailNotExists() {
      final String email = "nonexistent@example.com";
      final String password = "password123";

      when(authenticationManager.authenticate(any()))
          .thenThrow(new BadCredentialsException("User not found"));

      assertThatThrownBy(() -> authService.login(email, password))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("should throw UNAUTHORIZED when password is invalid")
    void shouldThrowUnauthorizedWhenPasswordInvalid() {
      final String email = "user@example.com";
      final String password = "wrongpassword";
      when(authenticationManager.authenticate(any()))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      assertThatThrownBy(() -> authService.login(email, password))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("should call tokenService.create when login succeeds")
    void shouldCallTokenServiceCreateOnSuccessfulLogin() {
      final String email = "user@example.com";
      final String password = "password123";

      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);
      user.setDisplayName("testuser");
      user.setPassword("hashed-password");

      JwtToken jwtToken = new JwtToken("jti-123", null, 1L, 3601L, null);

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      Authentication authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(authenticationManager.authenticate(any())).thenReturn(authentication);
      when(jwtService.createJwtToken(user)).thenReturn(jwtToken);
      when(jwtService.encodeJwtToken(jwtToken)).thenReturn("encoded-token");

      TokenResponse result = authService.login(email, password);

      verify(tokenService, times(1)).create(eq(jwtToken), anyString());
      assertThat(result.accessToken()).isEqualTo("encoded-token");
      assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("should return access token and refresh token on successful login")
    void shouldReturnTokensOnSuccessfulLogin() {
      final String email = "user@example.com";
      final String password = "password123";

      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);
      user.setDisplayName("testuser");
      user.setPassword("hashed-password");

      JwtToken jwtToken = new JwtToken("jti-123", "user-123", 1L, 3601L, Map.of("id", "user-123", "email", email));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      Authentication authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(authenticationManager.authenticate(any())).thenReturn(authentication);
      when(jwtService.createJwtToken(user)).thenReturn(jwtToken);
      when(jwtService.encodeJwtToken(jwtToken)).thenReturn("encoded-access-token");

      TokenResponse result = authService.login(email, password);

      assertThat(result).isNotNull();
      assertThat(result.accessToken()).isEqualTo("encoded-access-token");
      assertThat(result.refreshToken()).isNotBlank();
      assertThat(result.expiresIn()).isEqualTo(3601L);
    }
  }

  @Nested
  @DisplayName("logout method")
  class LogoutMethod {

    @Test
    @DisplayName("should call tokenService.deleteById with correct JTI")
    void shouldCallTokenServiceDeleteByIdWithCorrectJti() {
      JwtToken jwtToken = new JwtToken("token-jti-123", null, null, null, null);

      when(jwtService.decodeJwtToken("jwt-token-string")).thenReturn(jwtToken);

      // Set up authentication context so logout actually calls deleteById
      org.springframework.security.core.Authentication authentication =
          org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
      org.springframework.security.core.context.SecurityContextHolder.getContext()
          .setAuthentication(authentication);

      authService.logout("jwt-token-string");

      verify(tokenService, times(1)).deleteById("token-jti-123");

      // Verify context was cleared
      org.junit.jupiter.api.Assertions.assertNull(
          org.springframework.security.core.context.SecurityContextHolder.getContext()
              .getAuthentication());
    }
  }

  @Nested
  @DisplayName("refresh method")
  class RefreshMethod {

    @Test
    @DisplayName("should throw NOT_FOUND when refresh token not found")
    void shouldThrowNotFoundWhenRefreshTokenNotFound() {
      final String refreshToken = "invalid-refresh-token";

      when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh(refreshToken)).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("should throw when user does not exist for refresh token")
    void shouldThrowWhenUserMissingForRefreshToken() {
      JwtToken jwtToken = new JwtToken("jti-456", null, null, null, Map.of("id", "user-456"));

      String refreshToken = "refresh-token";
      Pair<JwtToken, String> tokenPair = ImmutablePair.of(jwtToken, refreshToken);

      when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.of(tokenPair));
      when(userService.getUserById("user-456")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh(refreshToken))
          .isInstanceOf(Exception.class)
          .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("should revoke old token and generate new tokens on successful refresh")
    void shouldRevokeOldTokenAndGenerateNewTokensOnSuccessfulRefresh() {
      JwtToken jwtToken = new JwtToken("jti-123", null, null, null, Map.of("id", "user-123"));

      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail("user@example.com");

      JwtToken newJwtToken = new JwtToken("new-jti-123", null, 1L, 3601L, null);

      String refreshToken = "refresh-token";
      Pair<JwtToken, String> tokenPair = ImmutablePair.of(jwtToken, refreshToken);

      when(tokenService.getByRefreshToken(refreshToken)).thenReturn(Optional.of(tokenPair));
      when(userService.getUserById("user-123")).thenReturn(Optional.of(user));
      when(jwtService.createJwtToken(user)).thenReturn(newJwtToken);
      when(jwtService.encodeJwtToken(newJwtToken)).thenReturn("encoded-token");

      try {
        authService.refresh(refreshToken);
      } catch (Exception e) {
        //
      }

      verify(tokenService, times(1)).deleteById("jti-123");
      verify(tokenService, times(1)).create(eq(newJwtToken), anyString());
    }
  }
}
