package hu.squarelabs.auth21.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.config.DynamoDbTableSchemaConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@DisplayName("UserRepository")
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = DynamoDbTableSchemaConfig.class)
class UserRepositoryTest {

  @Mock private DynamoDbEnhancedClient enhancedClient;

  @Mock private DynamoDbTable<UserEntity> userTable;

  @Autowired private TableSchema<UserEntity> userEntityTableSchema;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    when(enhancedClient.table(eq("users"), any())).thenReturn((DynamoDbTable) userTable);
    userRepository = new UserRepository(enhancedClient, userEntityTableSchema, "users");
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

    @Mock private DynamoDbIndex<UserEntity> emailIndex;

    @Test
    @DisplayName("should return user when user with email exists")
    void shouldReturnUserWhenUserWithEmailExists() {
      final String email = "test@example.com";
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);

      when(userTable.index("EmailIndex")).thenReturn(emailIndex);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity> mockPage =
          mock(software.amazon.awssdk.enhanced.dynamodb.model.Page.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.singletonList(mockPage).iterator();

      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(mockPage.items()).thenReturn(Collections.singletonList(user));
      when(emailIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByEmail(email);

      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo(email);
      verify(emailIndex, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String email = "error@example.com";

      when(userTable.index("EmailIndex")).thenReturn(emailIndex);
      when(emailIndex.query(any(QueryEnhancedRequest.class)))
          .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

      assertThatThrownBy(() -> userRepository.findByEmail(email))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to fetch user from database");

      verify(emailIndex, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should pass correct email to query filter")
    void shouldPassCorrectEmailToQueryFilter() {
      final String email = "test@example.com";

      when(userTable.index("EmailIndex")).thenReturn(emailIndex);

      ArgumentCaptor<QueryEnhancedRequest> captor =
          ArgumentCaptor.forClass(QueryEnhancedRequest.class);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.emptyIterator();
      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(emailIndex.query(captor.capture())).thenReturn(mockPageIterable);

      userRepository.findByEmail(email);

      assertThat(captor.getValue()).isNotNull();
      assertThat(captor.getValue().filterExpression()).isNotNull();
    }
  }

  @Nested
  @DisplayName("findByUserName method")
  class FindByUserNameMethod {

    @Mock private DynamoDbIndex<UserEntity> usernameIndex;

    @Test
    @DisplayName("should return user when user with username exists")
    void shouldReturnUserWhenUserWithUsernameExists() {
      final String username = "testuser";
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername(username);

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity> mockPage =
          mock(software.amazon.awssdk.enhanced.dynamodb.model.Page.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.singletonList(mockPage).iterator();

      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(mockPage.items()).thenReturn(Collections.singletonList(user));
      when(usernameIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByUserName(username);

      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo(username);
      verify(usernameIndex, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should return empty when user does not exist")
    void shouldReturnEmptyWhenUserDoesNotExist() {
      final String username = "nonexistent";

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.emptyIterator();
      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(usernameIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByUserName(username);

      assertThat(result).isEmpty();
      verify(usernameIndex, times(1)).query(any(QueryEnhancedRequest.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String username = "erroruser";

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);
      when(usernameIndex.query(any(QueryEnhancedRequest.class)))
          .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

      assertThatThrownBy(() -> userRepository.findByUserName(username))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to fetch user from database");

      verify(usernameIndex, times(1)).query(any(QueryEnhancedRequest.class));
    }
  }

  @Nested
  @DisplayName("findByUsernameOrEmail method")
  class FindByUsernameOrEmailMethod {

    @Mock private DynamoDbIndex<UserEntity> usernameIndex;
    @Mock private DynamoDbIndex<UserEntity> emailIndex;

    @Test
    @DisplayName("should return user when found by username")
    void shouldReturnUserWhenFoundByUsername() {
      final String username = "testuser";
      final String email = "test@example.com";
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setUsername(username);

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);

      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity> mockPage =
          mock(software.amazon.awssdk.enhanced.dynamodb.model.Page.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.singletonList(mockPage).iterator();

      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(mockPage.items()).thenReturn(Collections.singletonList(user));
      when(usernameIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByUsernameOrEmail(email, username);

      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("should return user when found by email only")
    void shouldReturnUserWhenFoundByEmailOnly() {
      final String username = "testuser";
      final String email = "test@example.com";
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail(email);

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);
      when(userTable.index("EmailIndex")).thenReturn(emailIndex);

      // Username query returns empty
      PageIterable<UserEntity> emptyPageIterable = mock(PageIterable.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> emptyPageIterator =
          Collections.emptyIterator();
      when(emptyPageIterable.iterator()).thenReturn(emptyPageIterator);
      when(usernameIndex.query(any(QueryEnhancedRequest.class))).thenReturn(emptyPageIterable);

      // Email query returns user
      PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
      software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity> mockPage =
          mock(software.amazon.awssdk.enhanced.dynamodb.model.Page.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> pageIterator =
          Collections.singletonList(mockPage).iterator();

      when(mockPageIterable.iterator()).thenReturn(pageIterator);
      when(mockPage.items()).thenReturn(Collections.singletonList(user));
      when(emailIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

      final var result = userRepository.findByUsernameOrEmail(email, username);

      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("should return empty when user not found by either username or email")
    void shouldReturnEmptyWhenNotFound() {
      final String username = "nonexistent";
      final String email = "nonexistent@example.com";

      when(userTable.index("UsernameIndex")).thenReturn(usernameIndex);
      when(userTable.index("EmailIndex")).thenReturn(emailIndex);

      PageIterable<UserEntity> emptyPageIterable = mock(PageIterable.class);
      Iterator<software.amazon.awssdk.enhanced.dynamodb.model.Page<UserEntity>> emptyPageIterator =
          Collections.emptyIterator();
      when(emptyPageIterable.iterator()).thenReturn(emptyPageIterator);
      when(usernameIndex.query(any(QueryEnhancedRequest.class))).thenReturn(emptyPageIterable);
      when(emailIndex.query(any(QueryEnhancedRequest.class))).thenReturn(emptyPageIterable);

      final var result = userRepository.findByUsernameOrEmail(email, username);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("save method")
  class SaveMethod {

    @Test
    @DisplayName("should save user entity")
    void shouldSaveUserEntity() {
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail("test@example.com");
      user.setUsername("testuser");

      doNothing().when(userTable).putItem(any(UserEntity.class));

      userRepository.save(user);

      ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userTable, times(1)).putItem(captor.capture());

      UserEntity savedUser = captor.getValue();
      assertThat(savedUser.getId()).isEqualTo("user-123");
      assertThat(savedUser.getCreatedAt()).isNotNull();
      assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should not override existing createdAt")
    void shouldNotOverrideExistingCreatedAt() {
      final UserEntity user = new UserEntity();
      user.setId("user-123");
      user.setEmail("test@example.com");
      user.setCreatedAt(java.time.Instant.parse("2024-01-01T00:00:00Z"));

      doNothing().when(userTable).putItem(any(UserEntity.class));

      userRepository.save(user);

      ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userTable, times(1)).putItem(captor.capture());

      assertThat(captor.getValue().getCreatedAt())
          .isEqualTo(java.time.Instant.parse("2024-01-01T00:00:00Z"));
    }
  }
}
