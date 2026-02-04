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

      assertThat(response.accessToken()).isEqualTo(accessToken);
      assertThat(response.refreshToken()).isEqualTo(refreshToken);
      assertThat(response.expiresIn()).isEqualTo(expiresIn);
      assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("should set token type to Bearer by default")
    void shouldSetTokenTypeToBearer() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response.tokenType()).isEqualTo("Bearer");
    }
  }

  @Nested
  @DisplayName("Getters")
  class Getters {

    @Test
    @DisplayName("should get access token")
    void shouldGetAccessToken() {
      final TokenResponse response = new TokenResponse("my-access", "my-refresh", 3600L);

      assertThat(response.accessToken()).isEqualTo("my-access");
    }

    @Test
    @DisplayName("should get refresh token")
    void shouldGetRefreshToken() {
      final TokenResponse response = new TokenResponse("access", "my-refresh", 3600L);

      assertThat(response.refreshToken()).isEqualTo("my-refresh");
    }

    @Test
    @DisplayName("should get token type")
    void shouldGetTokenType() {
      final TokenResponse response = new TokenResponse("access", "refresh", 3600L);

      assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("should get expires in")
    void shouldGetExpiresIn() {
      final TokenResponse response = new TokenResponse("access", "refresh", 7200L);

      assertThat(response.expiresIn()).isEqualTo(7200L);
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
}
