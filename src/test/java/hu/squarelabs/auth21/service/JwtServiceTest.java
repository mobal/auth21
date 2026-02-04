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
  private UserEntity testUser;
  private SimpleUserDetails testUserDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    String secretKey =
        Base64.getEncoder()
            .encodeToString("MySecretKeyForTestingPurposesOnly1234567890".getBytes());
    ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600L);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400L);

    testUser = new UserEntity();
    testUser.setId("user-123");
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("hashedPassword");
    testUser.setRoles(Collections.singletonList("USER"));
    testUserDetails = new SimpleUserDetails(testUser);
  }

  @Nested
  @DisplayName("generateToken")
  class GenerateToken {

    @Test
    @DisplayName("generates valid JWT token for UserDetails")
    void generatesValidJwtTokenForUserDetails() {
      String token = jwtService.generateToken(testUserDetails);

      assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generates token with extra claims")
    void generatesTokenWithExtraClaims() {
      Map<String, Object> extraClaims = Map.of("customClaim", "customValue");

      String token = jwtService.generateToken(extraClaims, testUserDetails);

      assertThat(token).isNotBlank();
    }
  }

  @Nested
  @DisplayName("generateRefreshToken")
  class GenerateRefreshToken {

    @Test
    @DisplayName("generates valid refresh token")
    void generatesValidRefreshToken() {
      String refreshToken = jwtService.generateRefreshToken(testUserDetails);

      assertThat(refreshToken).isNotBlank();
    }
  }

  @Nested
  @DisplayName("extractUserId")
  class ExtractUserId {

    @Test
    @DisplayName("extracts user ID from token")
    void extractsUserIdFromToken() {
      String token = jwtService.generateToken(testUserDetails);

      String userId = jwtService.extractUserId(token);

      assertThat(userId).isEqualTo("testuser");
    }
  }

  @Nested
  @DisplayName("extractExpiration")
  class ExtractExpiration {

    @Test
    @DisplayName("extracts expiration date from token")
    void extractsExpirationDateFromToken() {
      String token = jwtService.generateToken(testUserDetails);

      Date expiration = jwtService.extractExpiration(token);

      assertThat(expiration).isAfter(new Date());
    }
  }

  @Nested
  @DisplayName("isTokenValid")
  class IsTokenValid {

    @Test
    @DisplayName("returns true for valid token")
    void returnsTrueForValidToken() {
      testUser.setId("testuser");
      SimpleUserDetails userDetails = new SimpleUserDetails(testUser);
      String token = jwtService.generateToken(userDetails);

      boolean isValid = jwtService.isTokenValid(token, userDetails);

      assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("returns false for token with different username")
    void returnsFalseForTokenWithDifferentUsername() {
      testUser.setId("testuser");
      SimpleUserDetails userDetails = new SimpleUserDetails(testUser);
      String token = jwtService.generateToken(userDetails);

      UserEntity differentUser = new UserEntity();
      differentUser.setId("differentuser");
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
  @DisplayName("createJwtToken")
  class CreateJwtToken {

    @Test
    @DisplayName("creates JwtToken from UserEntity with all required fields")
    void createsJwtTokenFromUserEntityWithAllRequiredFields() {
      testUser.setDisplayName("Test User");

      JwtToken jwtToken = jwtService.createJwtToken(testUser);

      assertThat(jwtToken.jti()).isNotNull();
      assertThat(jwtToken.sub()).isEqualTo("user-123");
      assertThat(jwtToken.iat()).isNotNull();
      assertThat(jwtToken.exp()).isGreaterThan(jwtToken.iat());
      assertThat(jwtToken.user())
          .containsEntry("id", "user-123")
          .containsEntry("email", "test@example.com")
          .containsEntry("nickname", "Test User");
    }
  }

  @Nested
  @DisplayName("encodeJwtToken")
  class EncodeJwtToken {

    @Test
    @DisplayName("encodes JwtToken to string")
    void encodesJwtTokenToString() {
      JwtToken jwtToken =
          new JwtToken(
              "jti-123",
              "user-123",
              System.currentTimeMillis() / 1000,
              System.currentTimeMillis() / 1000 + 3600,
              Map.of("id", "user-123", "email", "test@example.com"));

      String encodedToken = jwtService.encodeJwtToken(jwtToken);

      assertThat(encodedToken).isNotBlank();
    }
  }

  @Nested
  @DisplayName("decodeJwtToken")
  class DecodeJwtToken {

    @Test
    @DisplayName("decodes JWT token string to JwtToken")
    void decodesJwtTokenStringToJwtToken() {
      JwtToken originalToken =
          new JwtToken(
              "jti-123",
              "user-123",
              System.currentTimeMillis() / 1000,
              System.currentTimeMillis() / 1000 + 3600,
              Map.of("id", "user-123", "email", "test@example.com"));

      String encodedToken = jwtService.encodeJwtToken(originalToken);
      JwtToken decodedToken = jwtService.decodeJwtToken(encodedToken);

      assertThat(decodedToken.sub()).isEqualTo("user-123");
      assertThat(decodedToken.user()).isNotNull();
    }
  }

  @Nested
  @DisplayName("extractClaim")
  class ExtractClaim {

    @Test
    @DisplayName("extracts custom claim from token")
    void extractsCustomClaimFromToken() {
      String token = jwtService.generateToken(testUserDetails);

      String subject = jwtService.extractClaim(token, Claims::getSubject);

      assertThat(subject).isEqualTo("testuser");
    }
  }
}
