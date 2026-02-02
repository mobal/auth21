package hu.squarelabs.auth21.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.exception.UserAlreadyExistsException;
import hu.squarelabs.auth21.model.entity.UserEntity;
import hu.squarelabs.auth21.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("UserService")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  @Nested
  @DisplayName("create method")
  class CreateMethod {
    @Test
    @DisplayName("should create new user successfully")
    void shouldCreateNewUser() {
      final String email = "test@example.com";
      final String password = "password123";
      final String username = "testuser";
      final String displayName = "Test User";
      when(userRepository.findByUsernameOrEmail(email, username)).thenReturn(Optional.empty());
      when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
      doNothing().when(userRepository).save(any(UserEntity.class));
      String userId = userService.create(email, password, username, displayName);
      assertThat(userId).isNotNull();
      assertThat(userId).isNotEmpty();
      ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userRepository).save(userCaptor.capture());
      UserEntity savedUser = userCaptor.getValue();
      assertThat(savedUser.getEmail()).isEqualTo(email);
      assertThat(savedUser.getUsername()).isEqualTo(username);
      assertThat(savedUser.getDisplayName()).isEqualTo(displayName);
      assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
      assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when user already exists")
    void shouldThrowExceptionWhenUserExists() {
      final String email = "existing@example.com";
      final String password = "password123";
      final String username = "existinguser";
      final String displayName = "Existing User";
      UserEntity existingUser = new UserEntity();
      existingUser.setId("existing-123");
      existingUser.setEmail(email);
      existingUser.setUsername(username);
      when(userRepository.findByUsernameOrEmail(email, username))
          .thenReturn(Optional.of(existingUser));
      assertThatThrownBy(() -> userService.create(email, password, username, displayName))
          .isInstanceOf(UserAlreadyExistsException.class);
      verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("should encode password before saving")
    void shouldEncodePassword() {
      final String email = "test@example.com";
      final String password = "plainPassword";
      final String username = "testuser";
      final String displayName = "Test User";
      when(userRepository.findByUsernameOrEmail(email, username)).thenReturn(Optional.empty());
      when(passwordEncoder.encode(password)).thenReturn("encodedPassword123");
      userService.create(email, password, username, displayName);
      verify(passwordEncoder).encode(password);
      ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword123");
    }
  }

  @Nested
  @DisplayName("getUserByEmail method")
  class GetUserByEmailMethod {
    @Test
    @DisplayName("should return user when found by email")
    void shouldReturnUserWhenFound() {
      final String email = "test@example.com";
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      Optional<UserEntity> result = userService.getUserByEmail(email);
      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo(email);
      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("should return empty when user not found")
    void shouldReturnEmptyWhenNotFound() {
      final String email = "nonexistent@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
      Optional<UserEntity> result = userService.getUserByEmail(email);
      assertThat(result).isEmpty();
      verify(userRepository).findByEmail(email);
    }
  }

  @Nested
  @DisplayName("getUserById method")
  class GetUserByIdMethod {
    @Test
    @DisplayName("should return user when found by ID")
    void shouldReturnUserWhenFound() {
      final String userId = "user-123";
      UserEntity user = new UserEntity();
      user.setId(userId);
      user.setEmail("test@example.com");
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      Optional<UserEntity> result = userService.getUserById(userId);
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(userId);
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("should return empty when user not found")
    void shouldReturnEmptyWhenNotFound() {
      final String userId = "nonexistent-123";
      when(userRepository.findById(userId)).thenReturn(Optional.empty());
      Optional<UserEntity> result = userService.getUserById(userId);
      assertThat(result).isEmpty();
      verify(userRepository).findById(userId);
    }
  }

  @Nested
  @DisplayName("getUserByUserName method")
  class GetUserByUserNameMethod {
    @Test
    @DisplayName("should return user when found by username")
    void shouldReturnUserWhenFound() {
      final String username = "testuser";
      UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername(username);
      when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
      Optional<UserEntity> result = userService.getUserByUserName(username);
      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo(username);
      verify(userRepository).findByUserName(username);
    }

    @Test
    @DisplayName("should return empty when user not found")
    void shouldReturnEmptyWhenNotFound() {
      final String username = "nonexistent";
      when(userRepository.findByUserName(username)).thenReturn(Optional.empty());
      Optional<UserEntity> result = userService.getUserByUserName(username);
      assertThat(result).isEmpty();
      verify(userRepository).findByUserName(username);
    }
  }
}
