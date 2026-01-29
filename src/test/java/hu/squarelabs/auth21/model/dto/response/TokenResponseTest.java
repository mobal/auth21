package hu.squarelabs.auth21.model.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TokenResponse")
class TokenResponseTest {

  @Nested
  @DisplayName("Constructor with parameters")
  class ConstructorWithParameters {

    @Test
    @DisplayName("should create TokenResponse with all fields")
    void shouldCreateTokenResponseWithAllFields() {
      final String accessToken = "access-token-123";
      final String refreshToken = "refresh-token-456";
      final Long expiresIn = 3600L;

      final TokenResponse response = new TokenResponse(accessToken, refreshToken, expiresIn);

      assertThat(response.getAccessToken()).isEqualTo(accessToken);
      assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
      assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
      assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("should set token type to Bearer by default")
    void shouldSetTokenTypeToBearer() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
  }

  @Nested
  @DisplayName("Getters")
  class Getters {

    @Test
    @DisplayName("should get access token")
    void shouldGetAccessToken() {
      final TokenResponse response = new TokenResponse("my-access", "my-refresh", 3600L);

      assertThat(response.getAccessToken()).isEqualTo("my-access");
    }

    @Test
    @DisplayName("should get refresh token")
    void shouldGetRefreshToken() {
      final TokenResponse response = new TokenResponse("access", "my-refresh", 3600L);

      assertThat(response.getRefreshToken()).isEqualTo("my-refresh");
    }

    @Test
    @DisplayName("should get token type")
    void shouldGetTokenType() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("should get expires in")
    void shouldGetExpiresIn() {
      final TokenResponse response = new TokenResponse("access", "refresh", 7200L);

      assertThat(response.getExpiresIn()).isEqualTo(7200L);
    }
  }

  @Nested
  @DisplayName("Setters")
  class Setters {

    @Test
    @DisplayName("should set access token")
    void shouldSetAccessToken() {
      final TokenResponse response = new TokenResponse("old-access", "refresh", 3600L);

      response.setAccessToken("new-access");

      assertThat(response.getAccessToken()).isEqualTo("new-access");
    }

    @Test
    @DisplayName("should set refresh token")
    void shouldSetRefreshToken() {
      final TokenResponse response = new TokenResponse("access", "old-refresh", 3600L);

      response.setRefreshToken("new-refresh");

      assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    @DisplayName("should set token type")
    void shouldSetTokenType() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      response.setTokenType("Custom");

      assertThat(response.getTokenType()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("should set expires in")
    void shouldSetExpiresIn() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      response.setExpiresIn(7200L);

      assertThat(response.getExpiresIn()).isEqualTo(7200L);
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal for same field values")
    void shouldBeEqualForSameFieldValues() {
      final TokenResponse response1 = new TokenResponse("access", "refresh", 3600L);
      final TokenResponse response2 = new TokenResponse("access", "refresh", 3600L);

      assertThat(response1).isEqualTo(response2);
    }

    @Test
    @DisplayName("should not be equal for different access token")
    void shouldNotBeEqualForDifferentAccessToken() {
      final TokenResponse response1 = new TokenResponse("access1", "refresh", 3600L);
      final TokenResponse response2 = new TokenResponse("access2", "refresh", 3600L);

      assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("should not be equal for different refresh token")
    void shouldNotBeEqualForDifferentRefreshToken() {
      final TokenResponse response1 = new TokenResponse("access", "refresh1", 3600L);
      final TokenResponse response2 = new TokenResponse("access", "refresh2", 3600L);

      assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("should not be equal for different expires in")
    void shouldNotBeEqualForDifferentExpiresIn() {
      final TokenResponse response1 = new TokenResponse("access", "refresh", 3600L);
      final TokenResponse response2 = new TokenResponse("access", "refresh", 7200L);

      assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("should not be equal to null")
    void shouldNotBeEqualToNull() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response).isNotEqualTo(null);
    }

    @Test
    @DisplayName("should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response).isNotEqualTo("not a token response");
    }
  }

  @Nested
  @DisplayName("Hash code")
  class HashCode {

    @Test
    @DisplayName("should have same hash code for equal objects")
    void shouldHaveSameHashCodeForEqualObjects() {
      final TokenResponse response1 = new TokenResponse("access", "refresh", 3600L);
      final TokenResponse response2 = new TokenResponse("access", "refresh", 3600L);

      assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("should likely have different hash code for different objects")
    void shouldLikelyHaveDifferentHashCodeForDifferentObjects() {
      final TokenResponse response1 = new TokenResponse("access1", "refresh", 3600L);
      final TokenResponse response2 = new TokenResponse("access2", "refresh", 3600L);

      assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }
  }

  @Nested
  @DisplayName("String representation")
  class StringRepresentation {

    @Test
    @DisplayName("should have non-empty toString")
    void shouldHaveNonEmptyToString() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      final String toString = response.toString();

      assertThat(toString).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("toString should contain class name")
    void toStringShouldContainClassName() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      final String toString = response.toString();

      assertThat(toString).contains("TokenResponse");
    }

    @Test
    @DisplayName("toString should contain field names or values")
    void toStringShouldContainFieldNamesOrValues() {
      final TokenResponse response = new TokenResponse("my-access", "my-refresh", 3600L);

      final String toString = response.toString();

      assertThat(toString).containsAnyOf("accessToken", "refreshToken", "my-access", "my-refresh");
    }
  }

  @Nested
  @DisplayName("No-arg constructor")
  class NoArgConstructor {

    @Test
    @DisplayName("should create instance with no-arg constructor")
    void shouldCreateInstanceWithNoArgConstructor() {
      final TokenResponse response = new TokenResponse();

      assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should allow field setting with no-arg constructor")
    void shouldAllowFieldSettingWithNoArgConstructor() {
      final TokenResponse response = new TokenResponse();
      response.setAccessToken("token");
      response.setRefreshToken("refresh");
      response.setExpiresIn(3600L);

      assertThat(response.getAccessToken()).isEqualTo("token");
      assertThat(response.getRefreshToken()).isEqualTo("refresh");
      assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }
  }
}
