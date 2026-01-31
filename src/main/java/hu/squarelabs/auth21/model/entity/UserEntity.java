package hu.squarelabs.auth21.model.entity;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class UserEntity {
  private String id;
  private String email;
  private String password;
  private String username;
  private String displayName;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant deletedAt;

  @DynamoDbPartitionKey
  @DynamoDbAttribute("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "EmailIndex")
  @DynamoDbAttribute("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @DynamoDbAttribute("password")
  public String getPassword() {
    return password;
  }

  public void setPassword(String passwordHash) {
    this.password = passwordHash;
  }

  @DynamoDbAttribute("username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @DynamoDbAttribute("display_name")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
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

  @DynamoDbAttribute("deleted_at")
  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
