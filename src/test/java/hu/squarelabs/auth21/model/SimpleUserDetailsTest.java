package hu.squarelabs.auth21.model;

import static org.assertj.core.api.Assertions.*;

import hu.squarelabs.auth21.model.entity.UserEntity;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SimpleUserDetails")
class SimpleUserDetailsTest {
  @Nested
  @DisplayName("getAuthorities method")
  class GetAuthoritiesMethod {
    @Test
    @DisplayName("should return authorities with ROLE_ prefix")
    void shouldReturnAuthoritiesWithRolePrefix() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Arrays.asList("USER", "ADMIN"));
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      var authorities = userDetails.getAuthorities();
      assertThat(authorities).hasSize(2);
      assertThat(authorities)
          .extracting("authority")
          .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("should return empty list when roles is null")
    void shouldReturnEmptyListWhenRolesIsNull() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(null);
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      var authorities = userDetails.getAuthorities();
      assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("should return empty list when roles is empty")
    void shouldReturnEmptyListWhenRolesIsEmpty() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Collections.emptyList());
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      var authorities = userDetails.getAuthorities();
      assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("should filter out null and blank roles")
    void shouldFilterOutNullAndBlankRoles() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Arrays.asList("USER", null, "", "  ", "ADMIN"));
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      var authorities = userDetails.getAuthorities();
      assertThat(authorities).hasSize(2);
      assertThat(authorities)
          .extracting("authority")
          .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
  }

  @Nested
  @DisplayName("getUserId method")
  class GetUserIdMethod {
    @Test
    @DisplayName("should return user ID")
    void shouldReturnUserId() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      assertThat(userDetails.getUserId()).isEqualTo("user-123");
    }
  }

  @Nested
  @DisplayName("getUsername method")
  class GetUsernameMethod {
    @Test
    @DisplayName("should return username")
    void shouldReturnUsername() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }
  }

  @Nested
  @DisplayName("getPassword method")
  class GetPasswordMethod {
    @Test
    @DisplayName("should return password")
    void shouldReturnPassword() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setPassword("hashedPassword");
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
    }
  }

  @Nested
  @DisplayName("isEnabled method")
  class IsEnabledMethod {
    @Test
    @DisplayName("should return true when deletedAt is null")
    void shouldReturnTrueWhenDeletedAtIsNull() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setDeletedAt(null);
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should return false when deletedAt is set")
    void shouldReturnFalseWhenDeletedAtIsSet() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setDeletedAt(Instant.now());
      SimpleUserDetails userDetails = new SimpleUserDetails(user);
      assertThat(userDetails.isEnabled()).isFalse();
    }
  }
}
