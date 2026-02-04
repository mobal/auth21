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
  @DisplayName("getAuthorities")
  class GetAuthorities {
    @Test
    @DisplayName("returns authorities with ROLE_ prefix")
    void returnsAuthoritiesWithRolePrefix() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Arrays.asList("USER", "ADMIN"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getAuthorities())
          .extracting("authority")
          .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("returns empty list when roles is null")
    void returnsEmptyListWhenRolesIsNull() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(null);

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("returns empty list when roles is empty")
    void returnsEmptyListWhenRolesIsEmpty() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Collections.emptyList());

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("filters out null and blank roles")
    void filtersOutNullAndBlankRoles() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setRoles(Arrays.asList("USER", null, "", "  ", "ADMIN"));

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getAuthorities())
          .extracting("authority")
          .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
  }

  @Nested
  @DisplayName("getUserId")
  class GetUserId {
    @Test
    @DisplayName("returns user ID")
    void returnsUserId() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getUserId()).isEqualTo("user-123");
    }
  }

  @Nested
  @DisplayName("getUsername")
  class GetUsername {
    @Test
    @DisplayName("returns username")
    void returnsUsername() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }
  }

  @Nested
  @DisplayName("getPassword")
  class GetPassword {
    @Test
    @DisplayName("returns password")
    void returnsPassword() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setPassword("hashedPassword");

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
    }
  }

  @Nested
  @DisplayName("isEnabled")
  class IsEnabled {
    @Test
    @DisplayName("returns true when user is not deleted")
    void returnsTrueWhenUserIsNotDeleted() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setDeletedAt(null);

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("returns false when user is deleted")
    void returnsFalseWhenUserIsDeleted() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setDeletedAt(Instant.now());

      SimpleUserDetails userDetails = new SimpleUserDetails(user);

      assertThat(userDetails.isEnabled()).isFalse();
    }
  }
}
