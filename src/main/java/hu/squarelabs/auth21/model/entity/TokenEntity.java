package hu.squarelabs.auth21.model.entity;

import hu.squarelabs.auth21.converter.MapAttributeConverter;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class TokenEntity {
  private String jti;
  private Map<String, Object> jwtToken;
  private String refreshToken;
  private Instant expiresAt;
  private Instant createdAt;
  private Instant updatedAt;
  private String userId;

  @DynamoDbPartitionKey
  @DynamoDbAttribute("jti")
  public String getJti() {
    return jti;
  }

  public void setJti(String jti) {
    this.jti = jti;
  }

  @DynamoDbConvertedBy(MapAttributeConverter.class)
  @DynamoDbAttribute("jwt_token")
  public Map<String, Object> getJwtToken() {
    return jwtToken;
  }

  public void setJwtToken(Map<String, Object> jwtToken) {
    this.jwtToken = jwtToken;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "RefreshTokenIndex")
  @DynamoDbAttribute("refresh_token")
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @DynamoDbAttribute("expires_at")
  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  @DynamoDbAttribute("created_at")
  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @DynamoDbAttribute("updated_at")
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "UserIdIndex")
  @DynamoDbAttribute("user_id")
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
