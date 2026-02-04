package hu.squarelabs.auth21.repository;

import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@Repository
public class UserRepository {
  private static final Logger logger = LogManager.getLogger(UserRepository.class);

  private static final AttributeValue NULL_ATTRIBUTE_VALUE =
      AttributeValue.builder().nul(true).build();

  private final DynamoDbTable<UserEntity> userTable;

  public UserRepository(
      DynamoDbEnhancedClient enhancedClient,
      TableSchema<UserEntity> userEntityTableSchema,
      @Value("${aws.dynamodb.table.users:users}") String tableName) {
    this.userTable = enhancedClient.table(tableName, userEntityTableSchema);
  }

  public void save(UserEntity userEntity) {
    if (userEntity.getCreatedAt() == null) {
      userEntity.setCreatedAt(java.time.Instant.now());
    }
    if (userEntity.getUpdatedAt() == null) {
      userEntity.setUpdatedAt(java.time.Instant.now());
    }
    userTable.putItem(userEntity);
  }

  public Optional<UserEntity> findById(String userId) {
    try {
      final var filterExpression =
          Expression.builder()
              .expression("attribute_not_exists(deleted_at) OR deleted_at = :null")
              .putExpressionValue(":null", NULL_ATTRIBUTE_VALUE)
              .build();

      final var queryRequest =
          QueryEnhancedRequest.builder()
              .queryConditional(
                  QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
              .filterExpression(filterExpression)
              .limit(1)
              .build();

      Iterator<UserEntity> results = userTable.query(queryRequest).items().iterator();

      return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
    } catch (DynamoDbException e) {
      logger.error("DynamoDB error fetching user by id {}: {}", userId, e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }

  public Optional<UserEntity> findByEmail(String email) {
    try {
      final var filterExpression =
          Expression.builder()
              .expression("attribute_not_exists(deleted_at) OR deleted_at = :null")
              .putExpressionValue(":null", NULL_ATTRIBUTE_VALUE)
              .build();

      final var queryRequest =
          QueryEnhancedRequest.builder()
              .queryConditional(
                  QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build()))
              .filterExpression(filterExpression)
              .limit(1)
              .build();

      final var pageIterator = userTable.index("EmailIndex").query(queryRequest).iterator();
      if (pageIterator.hasNext()) {
        final var page = pageIterator.next();
        final var itemIterator = page.items().iterator();
        if (itemIterator.hasNext()) {
          return Optional.of(itemIterator.next());
        }
      }

      return Optional.empty();
    } catch (DynamoDbException e) {
      logger.error("DynamoDB error fetching user by email {}: {}", email, e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }

  public Optional<UserEntity> findByUserName(String username) {
    try {
      final var filterExpression =
          Expression.builder()
              .expression("attribute_not_exists(deleted_at) OR deleted_at = :null")
              .putExpressionValue(":null", NULL_ATTRIBUTE_VALUE)
              .build();

      final var queryRequest =
          QueryEnhancedRequest.builder()
              .queryConditional(
                  QueryConditional.keyEqualTo(Key.builder().partitionValue(username).build()))
              .filterExpression(filterExpression)
              .limit(1)
              .build();

      final var pageIterator = userTable.index("UsernameIndex").query(queryRequest).iterator();
      if (pageIterator.hasNext()) {
        final var page = pageIterator.next();
        final var itemIterator = page.items().iterator();
        if (itemIterator.hasNext()) {
          return Optional.of(itemIterator.next());
        }
      }

      return Optional.empty();
    } catch (DynamoDbException e) {
      logger.error("DynamoDB error fetching user by username {}: {}", username, e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }

  public Optional<UserEntity> findByUsernameOrEmail(String email, String username) {
    try {
      return findByUserName(username).or(() -> findByEmail(email));
    } catch (DynamoDbException e) {
      logger.error(
          "DynamoDB error fetching user by username or email {} / {}: {}",
          username,
          email,
          e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }
}
