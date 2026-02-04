package hu.squarelabs.auth21.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.config.DynamoDbTableSchemaConfig;
import hu.squarelabs.auth21.model.entity.TokenEntity;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@DisplayName("TokenRepository")
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = DynamoDbTableSchemaConfig.class)
class TokenRepositoryTest {

  @Mock private DynamoDbEnhancedClient enhancedClient;

  @Mock private DynamoDbTable<TokenEntity> tokenTable;

  @Autowired private TableSchema<TokenEntity> tokenEntityTableSchema;

  private TokenRepository tokenRepository;

  @BeforeEach
  void setUp() {
    when(enhancedClient.table(eq("auth-tokens"), any())).thenReturn((DynamoDbTable) tokenTable);
    tokenRepository = new TokenRepository(enhancedClient, tokenEntityTableSchema, "auth-tokens");
  }

  @Nested
  @DisplayName("save method")
  class SaveMethod {

    @Test
    @DisplayName("should save token entity with timestamps")
    void shouldSaveTokenEntityWithTimestamps() {
      final TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("test-jti-123");
      tokenEntity.setRefreshToken("refresh-token-abc");
      tokenEntity.setJwtToken(Map.of("sub", "user-123"));
      tokenEntity.setExpiresAt(Instant.ofEpochSecond(5000L));
      tokenEntity.setUserId("user-123");

      tokenRepository.save(tokenEntity);

      assertThat(tokenEntity.getCreatedAt()).isNotNull();
      assertThat(tokenEntity.getUpdatedAt()).isNotNull();
      verify(tokenTable, times(1)).putItem(tokenEntity);
    }

    @Test
    @DisplayName("should not override existing createdAt timestamp")
    void shouldNotOverrideExistingCreatedAtTimestamp() {
      final Instant originalCreatedAt = Instant.ofEpochSecond(1000L);
      final TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("test-jti-123");
      tokenEntity.setRefreshToken("refresh-token-abc");
      tokenEntity.setJwtToken(Map.of("sub", "user-123"));
      tokenEntity.setExpiresAt(Instant.ofEpochSecond(5000L));
      tokenEntity.setUserId("user-123");
      tokenEntity.setCreatedAt(originalCreatedAt);

      tokenRepository.save(tokenEntity);

      assertThat(tokenEntity.getCreatedAt()).isEqualTo(originalCreatedAt);
      assertThat(tokenEntity.getUpdatedAt()).isNotNull();
      verify(tokenTable, times(1)).putItem(tokenEntity);
    }

    @Test
    @DisplayName("should not override existing updatedAt timestamp")
    void shouldNotOverrideExistingUpdatedAtTimestamp() {
      final Instant originalUpdatedAt = Instant.ofEpochSecond(2000L);
      final TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("test-jti-123");
      tokenEntity.setRefreshToken("refresh-token-abc");
      tokenEntity.setJwtToken(Map.of("sub", "user-123"));
      tokenEntity.setExpiresAt(Instant.ofEpochSecond(5000L));
      tokenEntity.setUserId("user-123");
      tokenEntity.setUpdatedAt(originalUpdatedAt);

      tokenRepository.save(tokenEntity);

      assertThat(tokenEntity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
      verify(tokenTable, times(1)).putItem(tokenEntity);
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("test-jti-123");

      doThrow(new RuntimeException("DynamoDB error")).when(tokenTable).putItem((TokenEntity) any());

      assertThatThrownBy(() -> tokenRepository.save(tokenEntity))
          .isInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  @DisplayName("findById method")
  class FindByIdMethod {

    @Test
    @DisplayName("should return token when token exists")
    void shouldReturnTokenWhenTokenExists() throws Exception {
      final String jti = "test-jti-123";
      final TokenEntity token = new TokenEntity();
      token.setJti(jti);
      token.setRefreshToken("refresh-token-abc");
      token.setUserId("user-123");

      when(tokenTable.getItem(any(Key.class))).thenReturn(token);

      final var result = tokenRepository.findById(jti);

      assertThat(result).isPresent();
      assertThat(result.get().getJti()).isEqualTo(jti);
      verify(tokenTable, times(1)).getItem(any(Key.class));
    }

    @Test
    @DisplayName("should return empty optional when token does not exist")
    void shouldReturnEmptyOptionalWhenTokenDoesNotExist() throws Exception {
      final String jti = "non-existent-jti";

      when(tokenTable.getItem(any(Key.class))).thenReturn(null);

      final var result = tokenRepository.findById(jti);

      assertThat(result).isEmpty();
      verify(tokenTable, times(1)).getItem(any(Key.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String jti = "error-jti";

      when(tokenTable.getItem(any(Key.class))).thenThrow(new RuntimeException("DynamoDB error"));

      assertThatThrownBy(() -> tokenRepository.findById(jti))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Error finding token by jti");
    }
  }

  @Nested
  @DisplayName("deleteById method")
  class DeleteByIdMethod {

    @Test
    @DisplayName("should delete token by jti")
    void shouldDeleteTokenByJti() throws Exception {
      final String jti = "test-jti-123";

      tokenRepository.deleteById(jti);

      verify(tokenTable, times(1)).deleteItem(any(Key.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String jti = "error-jti";

      doThrow(new RuntimeException("DynamoDB error")).when(tokenTable).deleteItem(any(Key.class));

      assertThatThrownBy(() -> tokenRepository.deleteById(jti))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Error deleting token");
    }
  }

  @Nested
  @DisplayName("findByRefreshToken method")
  class FindByRefreshTokenMethod {

    @Test
    @DisplayName("should call index query with correct table name")
    void shouldCallIndexQueryWithCorrectTableName() {
      final String refreshToken = "refresh-token-abc";

      when(tokenTable.index("RefreshTokenIndex"))
          .thenThrow(new RuntimeException("Index not found"));

      assertThatThrownBy(() -> tokenRepository.findByRefreshToken(refreshToken))
          .isInstanceOf(ResponseStatusException.class);

      verify(tokenTable, times(1)).index("RefreshTokenIndex");
    }

    @Test
    @DisplayName("should throw RuntimeException when DynamoDB operation fails")
    void shouldThrowRuntimeExceptionWhenDynamoDbOperationFails() {
      final String refreshToken = "error-refresh-token";

      when(tokenTable.index(anyString())).thenThrow(new RuntimeException("DynamoDB error"));

      assertThatThrownBy(() -> tokenRepository.findByRefreshToken(refreshToken))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Error finding token by refresh token");
    }

    @Test
    @DisplayName("should call query with QueryEnhancedRequest")
    void shouldCallQueryWithQueryEnhancedRequest() {
      final String refreshToken = "refresh-token-abc";

      when(tokenTable.index(anyString())).thenThrow(new RuntimeException("Mock error"));

      assertThatThrownBy(() -> tokenRepository.findByRefreshToken(refreshToken))
          .isInstanceOf(ResponseStatusException.class);

      verify(tokenTable).index("RefreshTokenIndex");
    }
  }
}
