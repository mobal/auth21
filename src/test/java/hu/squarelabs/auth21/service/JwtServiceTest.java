package hu.squarelabs.auth21.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.SimpleUserDetails;
import hu.squarelabs.auth21.model.entity.UserEntity;
import io.jsonwebtoken.Claims;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtService")
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  private JwtService jwtService;
  private String secretKey;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    // Generate a valid base64 encoded secret key (256 bits for HS256)
    secretKey =
        Base64.getEncoder()
            .encodeToString("MySecretKeyForTestingPurposesOnly1234567890".getBytes());
    ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600L);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400L);
  }

  @Nested
  @DisplayName("generateToken method")
  class GenerateTokenMethod {

    @Test
    @DisplayName("should generate valid JWT token for UserDetails")
    void shouldGenerateValidToken() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      String token = jwtService.generateToken(userDetails);

      assertThat(token).isNotNull();
      assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("should generate token with extra claims")
    void shouldGenerateTokenWithExtraClaims() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      Map<String, Object> extraClaims = Map.of("customClaim", "customValue");

      String token = jwtService.generateToken(extraClaims, userDetails);

      assertThat(token).isNotNull();
      assertThat(token).isNotEmpty();
    }
  }

  @Nested
  @DisplayName("generateRefreshToken method")
  class GenerateRefreshTokenMethod {

    @Test
    @DisplayName("should generate valid refresh token")
    void shouldGenerateValidRefreshToken() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      String refreshToken = jwtService.generateRefreshToken(userDetails);

      assertThat(refreshToken).isNotNull();
      assertThat(refreshToken).isNotEmpty();
    }
  }

  @Nested
  @DisplayName("extractUserId method")
  class ExtractUserIdMethod {

    @Test
    @DisplayName("should extract user ID from token")
    void shouldExtractUserId() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      String token = jwtService.generateToken(userDetails);

      String userId = jwtService.extractUserId(token);

      assertThat(userId).isEqualTo("testuser");
    }
  }

  @Nested
  @DisplayName("extractExpiration method")
  class ExtractExpirationMethod {

    @Test
    @DisplayName("should extract expiration date from token")
    void shouldExtractExpiration() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      String token = jwtService.generateToken(userDetails);

      Date expiration = jwtService.extractExpiration(token);

      assertThat(expiration).isNotNull();
      assertThat(expiration).isAfter(new Date());
    }
  }

  @Nested
  @DisplayName("isTokenValid method")
  class IsTokenValidMethod {

    @Test
    @DisplayName("should return true for valid token")
    void shouldReturnTrueForValidToken() {
      UserEntity user = new UserEntity();
      user.setId("testuser"); // Use same value as username
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      String token = jwtService.generateToken(userDetails);

      boolean isValid = jwtService.isTokenValid(token, userDetails);

      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("should return false for token with different username")
    void shouldReturnFalseForTokenWithDifferentUsername() {
      UserEntity user = new UserEntity();
      user.setId("testuser"); // Match username
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      String token = jwtService.generateToken(userDetails);

      // Create different user with different username
      UserEntity differentUser = new UserEntity();
      differentUser.setId("differentuser"); // Match username
      differentUser.setUsername("differentuser");
      differentUser.setEmail("different@example.com");
      differentUser.setPassword("hashedPassword");
      differentUser.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails differentUserDetails = new SimpleUserDetails(differentUser);

      boolean isValid = jwtService.isTokenValid(token, differentUserDetails);

      assertThat(isValid).isFalse();
    }
  }

  @Nested
  @DisplayName("createJwtToken method")
  class CreateJwtTokenMethod {

    @Test
    @DisplayName("should create JwtToken from UserEntity")
    void shouldCreateJwtToken() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setDisplayName("Test User");

      JwtToken jwtToken = jwtService.createJwtToken(user);

      assertThat(jwtToken).isNotNull();
      assertThat(jwtToken.jti()).isNotNull();
      assertThat(jwtToken.sub()).isEqualTo("user-123");
      assertThat(jwtToken.iat()).isNotNull();
      assertThat(jwtToken.exp()).isGreaterThan(jwtToken.iat());
      assertThat(jwtToken.user()).isNotNull();
      assertThat(jwtToken.user().get("id")).isEqualTo("user-123");
      assertThat(jwtToken.user().get("email")).isEqualTo("test@example.com");
      assertThat(jwtToken.user().get("nickname")).isEqualTo("Test User");
    }
  }

  @Nested
  @DisplayName("encodeJwtToken method")
  class EncodeJwtTokenMethod {

    @Test
    @DisplayName("should encode JwtToken to string")
    void shouldEncodeJwtToken() {
      JwtToken jwtToken =
          new JwtToken(
              "jti-123",
              "user-123",
              System.currentTimeMillis() / 1000,
              System.currentTimeMillis() / 1000 + 3600,
              Map.of("id", "user-123", "email", "test@example.com"));

      String encodedToken = jwtService.encodeJwtToken(jwtToken);

      assertThat(encodedToken).isNotNull();
      assertThat(encodedToken).isNotEmpty();
    }
  }

  @Nested
  @DisplayName("decodeJwtToken method")
  class DecodeJwtTokenMethod {

    @Test
    @DisplayName("should decode JWT token string to JwtToken")
    void shouldDecodeJwtToken() {
      JwtToken originalToken =
          new JwtToken(
              "jti-123",
              "user-123",
              System.currentTimeMillis() / 1000,
              System.currentTimeMillis() / 1000 + 3600,
              Map.of("id", "user-123", "email", "test@example.com"));

      String encodedToken = jwtService.encodeJwtToken(originalToken);
      JwtToken decodedToken = jwtService.decodeJwtToken(encodedToken);

      assertThat(decodedToken).isNotNull();
      assertThat(decodedToken.sub()).isEqualTo("user-123");
      assertThat(decodedToken.user()).isNotNull();
    }
  }

  @Nested
  @DisplayName("extractClaim method")
  class ExtractClaimMethod {

    @Test
    @DisplayName("should extract custom claim from token")
    void shouldExtractCustomClaim() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      String token = jwtService.generateToken(userDetails);

      String subject = jwtService.extractClaim(token, Claims::getSubject);

      assertThat(subject).isEqualTo("testuser");
    }
  }
}
