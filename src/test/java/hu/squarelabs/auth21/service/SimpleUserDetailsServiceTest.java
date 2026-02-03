package hu.squarelabs.auth21.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.model.SimpleUserDetails;
import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@DisplayName("SimpleUserDetailsService")
@ExtendWith(MockitoExtension.class)
class SimpleUserDetailsServiceTest {
  @Mock private UserService userService;
  @InjectMocks private SimpleUserDetailsService userDetailsService;

  @Nested
  @DisplayName("loadUserByUsername")
  class LoadUserByUsername {
    @Test
    @DisplayName("loads user by username when found")
    void loadsUserByUsernameWhenFound() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));
      when(userService.getUserByUserName("testuser")).thenReturn(Optional.of(user));

      var result = userDetailsService.loadUserByUsername("testuser");

      assertThat(result).isInstanceOf(SimpleUserDetails.class);
      assertThat(result.getUsername()).isEqualTo("testuser");
      verify(userService).getUserByUserName("testuser");
    }

    @Test
    @DisplayName("loads user by email when username not found")
    void loadsUserByEmailWhenUsernameNotFound() {
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));
      when(userService.getUserByUserName("test@example.com")).thenReturn(Optional.empty());
      when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

      var result = userDetailsService.loadUserByUsername("test@example.com");

      assertThat(result).isInstanceOf(SimpleUserDetails.class);
      verify(userService).getUserByUserName("test@example.com");
      verify(userService).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("throws UsernameNotFoundException when user not found")
    void throwsUsernameNotFoundExceptionWhenUserNotFound() {
      when(userService.getUserByUserName("nonexistent")).thenReturn(Optional.empty());
      when(userService.getUserByEmail("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
          .isInstanceOf(UsernameNotFoundException.class)
          .hasMessageContaining("nonexistent");

      verify(userService).getUserByUserName("nonexistent");
      verify(userService).getUserByEmail("nonexistent");
    }
  }
}
