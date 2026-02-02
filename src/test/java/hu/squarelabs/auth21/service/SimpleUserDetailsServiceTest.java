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
  @DisplayName("loadUserByUsername method")
  class LoadUserByUsernameMethod {
    @Test
    @DisplayName("should load user by username successfully")
    void shouldLoadUserByUsername() {
      final String username = "testuser";
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername(username);
      user.setEmail("test@example.com");
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));
      when(userService.getUserByUserName(username)).thenReturn(Optional.of(user));
      var result = userDetailsService.loadUserByUsername(username);
      assertThat(result).isNotNull();
      assertThat(result).isInstanceOf(SimpleUserDetails.class);
      assertThat(result.getUsername()).isEqualTo(username);
      verify(userService).getUserByUserName(username);
    }

    @Test
    @DisplayName("should load user by email when username not found")
    void shouldLoadUserByEmail() {
      final String email = "test@example.com";
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername("testuser");
      user.setEmail(email);
      user.setPassword("hashedPassword");
      user.setRoles(Collections.singletonList("USER"));
      when(userService.getUserByUserName(email)).thenReturn(Optional.empty());
      when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
      var result = userDetailsService.loadUserByUsername(email);
      assertThat(result).isNotNull();
      assertThat(result).isInstanceOf(SimpleUserDetails.class);
      verify(userService).getUserByUserName(email);
      verify(userService).getUserByEmail(email);
    }

    @Test
    @DisplayName("should throw UserNotFoundException when user not found")
    void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
      final String username = "nonexistent";
      when(userService.getUserByUserName(username)).thenReturn(Optional.empty());
      when(userService.getUserByEmail(username)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
          .isInstanceOf(UsernameNotFoundException.class)
          .hasMessageContaining(username);
      verify(userService).getUserByUserName(username);
      verify(userService).getUserByEmail(username);
    }
  }
}
