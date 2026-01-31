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

  private final DynamoDbEnhancedClient enhancedClient;
  private final DynamoDbTable<UserEntity> userTable;
  private final String tableName;

  public UserRepository(
      DynamoDbEnhancedClient enhancedClient,
      @Value("${aws.dynamodb.table.users:users}") String tableName) {
    this.enhancedClient = enhancedClient;
    this.tableName = tableName;
    this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(UserEntity.class));
  }

  public Optional<UserEntity> findById(String userId) {
    try {
      final var nullAttr = AttributeValue.builder().nul(true).build();

      final var filterExpression =
          Expression.builder()
              .expression("deleted_at = :null")
              .putExpressionValue(":null", nullAttr)
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
      final var emailAttr = AttributeValue.builder().s(email).build();
      final var nullAttr = AttributeValue.builder().nul(true).build();

      final var filterExpression =
          Expression.builder()
              .expression("deleted_at = :null AND email = :email")
              .putExpressionValue(":null", nullAttr)
              .putExpressionValue(":email", emailAttr)
              .build();

      final var scanRequest =
          ScanEnhancedRequest.builder().filterExpression(filterExpression).limit(1).build();

      Iterator<UserEntity> results = userTable.scan(scanRequest).items().iterator();

      return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
    } catch (DynamoDbException e) {
      logger.error("DynamoDB error fetching user by email {}: {}", email, e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }

  public Optional<UserEntity> findByUserName(String username) {
    try {
      final var usernameAttr = AttributeValue.builder().s(username).build();

      final var filterExpression =
          Expression.builder()
              .expression("username = :username AND attribute_not_exists(deleted_at)")
              .putExpressionValue(":username", usernameAttr)
              .build();

      final var scanRequest =
          ScanEnhancedRequest.builder().filterExpression(filterExpression).limit(1).build();

      Iterator<UserEntity> results = userTable.scan(scanRequest).items().iterator();

      return results.hasNext() ? Optional.of(results.next()) : Optional.empty();
    } catch (DynamoDbException e) {
      logger.error("DynamoDB error fetching user by username {}: {}", username, e.getMessage());
      throw new RuntimeException("Failed to fetch user from database", e);
    }
  }
}
