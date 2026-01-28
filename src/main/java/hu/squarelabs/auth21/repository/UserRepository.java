package hu.squarelabs.auth21.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import hu.squarelabs.auth21.model.entity.UserEntity;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

@Repository
public class UserRepository {

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
            final var user = userTable.getItem(Key.builder()
                    .partitionValue(userId)
                    .build());
            if (user != null && user.getDeletedAt() != null) {
                return Optional.empty();
            }

            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user by id: " + userId, e);
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        try {
            final var emailAttr = AttributeValue.builder().s(email).build();

            final var filterExpression = Expression.builder()
                    .expression("email = :email AND attribute_not_exists(deleted_at)")
                    .putExpressionValue(":email", emailAttr)
                    .build();

            final var scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(filterExpression)
                    .limit(1)
                    .build();

            Iterator<UserEntity> results = userTable.scan(scanRequest).items().iterator();

            return results.hasNext() ? Optional.of(results.next()) : Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching user by email: " + email, e);
        }
    }
}
