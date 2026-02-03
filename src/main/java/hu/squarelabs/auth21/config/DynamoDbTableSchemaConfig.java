package hu.squarelabs.auth21.config;

import hu.squarelabs.auth21.converter.MapAttributeConverter;
import hu.squarelabs.auth21.model.entity.TokenEntity;
import hu.squarelabs.auth21.model.entity.UserEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.ListAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.StringAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Configuration
public class DynamoDbTableSchemaConfig {

  @Bean
  public TableSchema<UserEntity> userEntityTableSchema() {
    return StaticTableSchema.builder(UserEntity.class)
        .newItemSupplier(UserEntity::new)
        .addAttribute(
            String.class,
            a ->
                a.name("id")
                    .getter(UserEntity::getId)
                    .setter(UserEntity::setId)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(
            String.class,
            a ->
                a.name("email")
                    .getter(UserEntity::getEmail)
                    .setter(UserEntity::setEmail)
                    .tags(StaticAttributeTags.secondaryPartitionKey("EmailIndex")))
        .addAttribute(
            String.class,
            a -> a.name("password").getter(UserEntity::getPassword).setter(UserEntity::setPassword))
        .addAttribute(
            String.class,
            a ->
                a.name("username")
                    .getter(UserEntity::getUsername)
                    .setter(UserEntity::setUsername)
                    .tags(StaticAttributeTags.secondaryPartitionKey("UsernameIndex")))
        .addAttribute(
            String.class,
            a ->
                a.name("display_name")
                    .getter(UserEntity::getDisplayName)
                    .setter(UserEntity::setDisplayName))
        .addAttribute(
            EnhancedType.listOf(String.class),
            a ->
                a.name("roles")
                    .getter(UserEntity::getRoles)
                    .setter(UserEntity::setRoles)
                    .attributeConverter(
                        ListAttributeConverter.builder(EnhancedType.listOf(String.class))
                            .collectionConstructor(ArrayList::new)
                            .elementConverter(StringAttributeConverter.create())
                            .build()))
        .addAttribute(
            Instant.class,
            a ->
                a.name("created_at")
                    .getter(UserEntity::getCreatedAt)
                    .setter(UserEntity::setCreatedAt))
        .addAttribute(
            Instant.class,
            a ->
                a.name("updated_at")
                    .getter(UserEntity::getUpdatedAt)
                    .setter(UserEntity::setUpdatedAt))
        .addAttribute(
            Instant.class,
            a ->
                a.name("deleted_at")
                    .getter(UserEntity::getDeletedAt)
                    .setter(UserEntity::setDeletedAt))
        .build();
  }

  @Bean
  @SuppressWarnings({"unchecked", "rawtypes"})
  public TableSchema<TokenEntity> tokenEntityTableSchema() {
    AttributeConverter<Map<String, Object>> mapConverter = new MapAttributeConverter();

    return StaticTableSchema.builder(TokenEntity.class)
        .newItemSupplier(TokenEntity::new)
        .addAttribute(
            String.class,
            a ->
                a.name("jti")
                    .getter(TokenEntity::getJti)
                    .setter(TokenEntity::setJti)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(
            Map.class,
            a ->
                a.name("jwt_token")
                    .getter(TokenEntity::getJwtToken)
                    .setter(TokenEntity::setJwtToken)
                    .attributeConverter((AttributeConverter) mapConverter))
        .addAttribute(
            String.class,
            a ->
                a.name("refresh_token")
                    .getter(TokenEntity::getRefreshToken)
                    .setter(TokenEntity::setRefreshToken)
                    .tags(StaticAttributeTags.secondaryPartitionKey("RefreshTokenIndex")))
        .addAttribute(
            Instant.class,
            a ->
                a.name("expires_at")
                    .getter(TokenEntity::getExpiresAt)
                    .setter(TokenEntity::setExpiresAt))
        .addAttribute(
            Instant.class,
            a ->
                a.name("created_at")
                    .getter(TokenEntity::getCreatedAt)
                    .setter(TokenEntity::setCreatedAt))
        .addAttribute(
            Instant.class,
            a ->
                a.name("updated_at")
                    .getter(TokenEntity::getUpdatedAt)
                    .setter(TokenEntity::setUpdatedAt))
        .addAttribute(
            String.class,
            a ->
                a.name("user_id")
                    .getter(TokenEntity::getUserId)
                    .setter(TokenEntity::setUserId)
                    .tags(StaticAttributeTags.secondaryPartitionKey("UserIdIndex")))
        .build();
  }
}
