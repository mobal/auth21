package hu.squarelabs.auth21.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.Collections;
import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@DisplayName("UserRepository")
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

  @Mock private DynamoDbEnhancedClient enhancedClient;

  @Mock private DynamoDbTable<UserEntity> userTable;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    when(enhancedClient.table(eq("users"), any())).thenReturn((DynamoDbTable) userTable);
    userRepository = new UserRepository(enhancedClient, "users");
  }

  @Nested
  @DisplayName("findById method")
  class FindByIdMethod {

    @Test
    @DisplayName("should return user when user exists")
    void shouldReturnUserWhenUserExists() {
      final String userId = "user-123";
      final UserEntity user = new UserEntity();
      user.setId(userId);
      user.setEmail("test@example.com");

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<UserEntity> mockIterator = Collections.singletonList(user).iterator();
      when(mockPageIterable.items()).thenReturn(() -> mockIterator);
      when(userTable.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findById(userId);

      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(userId);
      assertThat(result.get().getEmail()).isEqualTo("test@example.com");
      verify(userTable, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should return empty optional when user does not exist")
    void shouldReturnEmptyOptionalWhenUserDoesNotExist() {
      final String userId = "non-existent-user";

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<UserEntity> mockIterator = Collections.emptyIterator();
      when(mockPageIterable.items()).thenReturn(() -> mockIterator);
      when(userTable.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findById(userId);

      assertThat(result).isEmpty();
      verify(userTable, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should return empty optional when user is deleted")
    void shouldReturnEmptyOptionalWhenUserIsDeleted() {
      final String userId = "deleted-user-123";

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<UserEntity> mockIterator = Collections.emptyIterator();
      when(mockPageIterable.items()).thenReturn(() -> mockIterator);
      when(userTable.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findById(userId);

      assertThat(result).isEmpty();
      verify(userTable, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String userId = "error-user";

      when(userTable.query(any(QueryEnhancedRequest.class)))
          .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

      assertThatThrownBy(() -> userRepository.findById(userId))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to fetch user from database");
    }
  }

  @Nested
  @DisplayName("findByEmail method")
  class FindByEmailMethod {

    @Test
    @DisplayName("should return user when user with email exists")
    void shouldReturnUserWhenUserWithEmailExists() {
      final String email = "test@example.com";
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<UserEntity> mockIterator = Collections.singletonList(user).iterator();
      when(mockPageIterable.items()).thenReturn(() -> mockIterator);
      when(userTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByEmail(email);

      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo(email);
      verify(userTable, times(1)).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String email = "error@example.com";

      when(userTable.scan(any(ScanEnhancedRequest.class)))
          .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

      assertThatThrownBy(() -> userRepository.findByEmail(email))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to fetch user from database");

      verify(userTable, times(1)).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    @DisplayName("should pass correct email to scan filter")
    void shouldPassCorrectEmailToScanFilter() {
      final String email = "test@example.com";

      ArgumentCaptor<ScanEnhancedRequest> captor =
          ArgumentCaptor.forClass(ScanEnhancedRequest.class);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<UserEntity> mockIterator = Collections.emptyIterator();
      when(mockPageIterable.items()).thenReturn(() -> mockIterator);
      when(userTable.scan(captor.capture())).thenReturn(mockPageIterable);

      userRepository.findByEmail(email);

      assertThat(captor.getValue()).isNotNull();
      assertThat(captor.getValue().filterExpression()).isNotNull();
    }
  }
}
