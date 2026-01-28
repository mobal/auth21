package hu.squarelabs.auth21.repository;

import hu.squarelabs.auth21.model.entity.TokenEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

@Repository
public class TokenRepository {
    private final DynamoDbTable<TokenEntity> tokenTable;

    public TokenRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table.tokens:auth-tokens}") String tableName) {
        this.tokenTable = enhancedClient.table(tableName,
                TableSchema.fromBean(TokenEntity.class));
    }

    public void save(TokenEntity tokenEntity) {
        if (tokenEntity.getCreatedAt() == null) {
            tokenEntity.setCreatedAt(Instant.now());
        }
        if (tokenEntity.getUpdatedAt() == null) {
            tokenEntity.setUpdatedAt(Instant.now());
        }
        tokenTable.putItem(tokenEntity);
    }

    public Optional<TokenEntity> findById(String jti) {
        try {
            TokenEntity token = tokenTable.getItem(Key.builder()
                    .partitionValue(jti)
                    .build());
            return Optional.ofNullable(token);
        } catch (Exception e) {
            throw new RuntimeException("Error finding token by jti: " + jti, e);
        }
    }

    public void deleteById(String jti) {
        try {
            tokenTable.deleteItem(Key.builder()
                    .partitionValue(jti)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting token: " + jti, e);
        }
    }

    public Optional<TokenEntity> findByRefreshToken(String refreshToken) {
        try {
            DynamoDbIndex<TokenEntity> refreshTokenIndex = tokenTable.index("RefreshTokenIndex");
            QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional.keyEqualTo(
                            Key.builder().partitionValue(refreshToken).build()))
                    .limit(1)
                    .build();

            final Iterator<Page<TokenEntity>> pageiTerator = refreshTokenIndex.query(queryRequest).iterator();
            if (pageiTerator.hasNext()) {
                Page<TokenEntity> page = pageiTerator.next();
                Iterator<TokenEntity> itemIterator = page.items().iterator();
                if (itemIterator.hasNext()) {
                    return Optional.of(itemIterator.next());
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error finding token by refresh token", e);
        }
    }
}
