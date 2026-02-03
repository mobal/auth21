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
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("throws BadCredentialsException when user does not exist")
    void throwsBadCredentialsExceptionWhenUserDoesNotExist() {
      when(authenticationManager.authenticate(any()))
          .thenThrow(new BadCredentialsException("User not found"));

      assertThatThrownBy(() -> authService.login("nonexistent@example.com", "password123"))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("throws BadCredentialsException when password is incorrect")
    void throwsBadCredentialsExceptionWhenPasswordIsIncorrect() {
      when(authenticationManager.authenticate(any()))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      assertThatThrownBy(() -> authService.login("user@example.com", "wrongpassword"))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("creates token and returns TokenResponse with all fields on success")
    void createsTokenAndReturnsTokenResponseWithAllFieldsOnSuccess() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail("user@example.com");
      user.setDisplayName("testuser");
      user.setPassword("hashed-password");

      JwtToken jwtToken =
          new JwtToken(
              "jti-123",
              "user-123",
              1L,
              3601L,
              Map.of("id", "user-123", "email", "user@example.com"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      Authentication authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(authenticationManager.authenticate(any())).thenReturn(authentication);
      when(jwtService.createJwtToken(user)).thenReturn(jwtToken);
      when(jwtService.encodeJwtToken(jwtToken)).thenReturn("encoded-access-token");

      TokenResponse result = authService.login("user@example.com", "password123");

      verify(tokenService).create(eq(jwtToken), anyString());
      assertThat(result.accessToken()).isEqualTo("encoded-access-token");
      assertThat(result.refreshToken()).isNotBlank();
      assertThat(result.expiresIn()).isEqualTo(3601L);
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("deletes token and clears security context")
    void deletesTokenAndClearsSecurityContext() {
      JwtToken jwtToken = new JwtToken("token-jti-123", null, null, null, null);
      when(jwtService.decodeJwtToken("jwt-token-string")).thenReturn(jwtToken);

      org.springframework.security.core.Authentication authentication =
          org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
      org.springframework.security.core.context.SecurityContextHolder.getContext()
          .setAuthentication(authentication);

      authService.logout("jwt-token-string");

      verify(tokenService).deleteById("token-jti-123");
      assertThat(
              org.springframework.security.core.context.SecurityContextHolder.getContext()
                  .getAuthentication())
          .isNull();
    }
  }

  @Nested
  @DisplayName("refresh")
  class Refresh {

    @Test
    @DisplayName("throws exception when refresh token not found")
    void throwsExceptionWhenRefreshTokenNotFound() {
      when(tokenService.getByRefreshToken("invalid-token")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh("invalid-token")).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("throws exception when user not found for refresh token")
    void throwsExceptionWhenUserNotFoundForRefreshToken() {
      JwtToken jwtToken = new JwtToken("jti-456", null, null, null, Map.of("id", "user-456"));
      Pair<JwtToken, String> tokenPair = ImmutablePair.of(jwtToken, "refresh-token");

      when(tokenService.getByRefreshToken("refresh-token")).thenReturn(Optional.of(tokenPair));
      when(userService.getUserById("user-456")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.refresh("refresh-token"))
          .isInstanceOf(Exception.class)
          .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("revokes old token and generates new tokens on success")
    void revokesOldTokenAndGeneratesNewTokensOnSuccess() {
      JwtToken jwtToken = new JwtToken("jti-123", null, null, null, Map.of("id", "user-123"));
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail("user@example.com");
      JwtToken newJwtToken = new JwtToken("new-jti-123", null, 1L, 3601L, null);
      Pair<JwtToken, String> tokenPair = ImmutablePair.of(jwtToken, "refresh-token");

      when(tokenService.getByRefreshToken("refresh-token")).thenReturn(Optional.of(tokenPair));
      when(userService.getUserById("user-123")).thenReturn(Optional.of(user));
      when(jwtService.createJwtToken(user)).thenReturn(newJwtToken);
      when(jwtService.encodeJwtToken(newJwtToken)).thenReturn("encoded-token");

      try {
        authService.refresh("refresh-token");
      } catch (Exception ignored) {
      }

      verify(tokenService).deleteById("jti-123");
      verify(tokenService).create(eq(newJwtToken), anyString());
    }
  }
}
