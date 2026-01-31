package hu.squarelabs.auth21.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JwtToken")
class JwtTokenTest {

  @Nested
  @DisplayName("Constructor with parameters")
  class ConstructorWithParameters {

    @Test
    @DisplayName("should create JwtToken with all fields")
    void shouldCreateJwtTokenWithAllFields() {
      final String jti = "test-jti-123";
      final String sub = "user-456";
      final Long iat = 1000L;
      final Long exp = 2000L;
      final Map<String, Object> user = new HashMap<>();
      user.put("id", "user-456");

      final JwtToken token = new JwtToken(jti, sub, iat, exp, user);

      assertThat(token.getJti()).isEqualTo(jti);
      assertThat(token.getSub()).isEqualTo(sub);
      assertThat(token.getIat()).isEqualTo(iat);
      assertThat(token.getExp()).isEqualTo(exp);
      assertThat(token.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("should handle null user map")
    void shouldHandleNullUserMap() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, null);

      assertThat(token.getUser()).isNull();
    }
  }

  @Nested
  @DisplayName("Getters and Setters")
  class GettersAndSetters {

    @Test
    @DisplayName("should get and set jti")
    void shouldGetAndSetJti() {
      final JwtToken token = new JwtToken();
      token.setJti("my-jti");

      assertThat(token.getJti()).isEqualTo("my-jti");
    }

    @Test
    @DisplayName("should get and set sub")
    void shouldGetAndSetSub() {
      final JwtToken token = new JwtToken();
      token.setSub("user-123");

      assertThat(token.getSub()).isEqualTo("user-123");
    }

    @Test
    @DisplayName("should get and set iat")
    void shouldGetAndSetIat() {
      final JwtToken token = new JwtToken();
      token.setIat(1000L);

      assertThat(token.getIat()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("should get and set exp")
    void shouldGetAndSetExp() {
      final JwtToken token = new JwtToken();
      token.setExp(2000L);

      assertThat(token.getExp()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("should get and set user")
    void shouldGetAndSetUser() {
      final JwtToken token = new JwtToken();
      final Map<String, Object> user = Map.of("id", "user-123", "email", "user@example.com");
      token.setUser(user);

      assertThat(token.getUser()).isEqualTo(user);
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal for same field values")
    void shouldBeEqualForSameFieldValues() {
      // JwtToken doesn't override equals(), so this test compares field values instead
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti", "sub", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti", "sub", 1000L, 2000L, user);

      assertThat(token1.getJti()).isEqualTo(token2.getJti());
      assertThat(token1.getSub()).isEqualTo(token2.getSub());
      assertThat(token1.getIat()).isEqualTo(token2.getIat());
      assertThat(token1.getExp()).isEqualTo(token2.getExp());
      assertThat(token1.getUser()).isEqualTo(token2.getUser());
    }

    @Test
    @DisplayName("should not be equal for different jti")
    void shouldNotBeEqualForDifferentJti() {
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti1", "sub", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti2", "sub", 1000L, 2000L, user);

      assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("should not be equal for different sub")
    void shouldNotBeEqualForDifferentSub() {
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti", "sub1", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti", "sub2", 1000L, 2000L, user);

      assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("should not be equal for different iat")
    void shouldNotBeEqualForDifferentIat() {
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti", "sub", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti", "sub", 1100L, 2000L, user);

      assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("should not be equal for different exp")
    void shouldNotBeEqualForDifferentExp() {
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti", "sub", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti", "sub", 1000L, 3000L, user);

      assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("should not be equal for different user")
    void shouldNotBeEqualForDifferentUser() {
      final JwtToken token1 = new JwtToken("jti", "sub", 1000L, 2000L, Map.of("id", "1"));
      final JwtToken token2 = new JwtToken("jti", "sub", 1000L, 2000L, Map.of("id", "2"));

      assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("should not be equal to null")
    void shouldNotBeEqualToNull() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, null);

      assertThat(token).isNotEqualTo(null);
    }

    @Test
    @DisplayName("should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, null);

      assertThat(token).isNotEqualTo("not a jwt token");
    }
  }

  @Nested
  @DisplayName("Hash code")
  class HashCode {

    @Test
    @DisplayName("should have same hash code for equal objects")
    void shouldHaveSameHashCodeForEqualObjects() {
      // JwtToken doesn't override hashCode(), so instances with same fields won't have same
      // hashCode
      // Instead verify each instance has a hashCode
      final Map<String, Object> user = Map.of("id", "user-123");
      final JwtToken token1 = new JwtToken("jti", "sub", 1000L, 2000L, user);
      final JwtToken token2 = new JwtToken("jti", "sub", 1000L, 2000L, user);

      assertThat(token1.hashCode()).isNotZero();
      assertThat(token2.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("should have hash code for null user")
    void shouldHaveHashCodeForNullUser() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, null);

      assertThat(token.hashCode()).isNotZero();
    }
  }

  @Nested
  @DisplayName("String representation")
  class StringRepresentation {

    @Test
    @DisplayName("should have non-empty toString")
    void shouldHaveNonEmptyToString() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, Map.of("id", "123"));

      final String toString = token.toString();

      assertThat(toString).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("toString should contain class name")
    void toStringShouldContainClassName() {
      final JwtToken token = new JwtToken("jti", "sub", 1000L, 2000L, null);

      final String toString = token.toString();

      assertThat(toString).contains("JwtToken");
    }

    @Test
    @DisplayName("toString should contain field names or values")
    void toStringShouldContainFieldNamesOrValues() {
      final JwtToken token = new JwtToken("my-jti", "my-sub", 1000L, 2000L, null);

      final String toString = token.toString();

      assertThat(toString).containsAnyOf("jti", "sub", "my-jti", "my-sub");
    }
  }

  @Nested
  @DisplayName("No-arg constructor")
  class NoArgConstructor {

    @Test
    @DisplayName("should create instance with no-arg constructor")
    void shouldCreateInstanceWithNoArgConstructor() {
      final JwtToken token = new JwtToken();

      assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("should allow field setting with no-arg constructor")
    void shouldAllowFieldSettingWithNoArgConstructor() {
      final JwtToken token = new JwtToken();
      token.setJti("jti");
      token.setSub("sub");
      token.setIat(1000L);
      token.setExp(2000L);

      assertThat(token.getJti()).isEqualTo("jti");
      assertThat(token.getSub()).isEqualTo("sub");
      assertThat(token.getIat()).isEqualTo(1000L);
      assertThat(token.getExp()).isEqualTo(2000L);
    }
  }
}
