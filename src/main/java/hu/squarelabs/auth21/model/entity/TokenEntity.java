package hu.squarelabs.auth21.model.entity;

import java.time.Instant;
import java.util.Map;

public class TokenEntity {
  private String jti;
  private Map<String, Object> jwtToken;
  private String refreshToken;
  private Instant expiresAt;
  private Instant createdAt;
  private Instant updatedAt;
  private String userId;

  public String getJti() {
    return jti;
  }

  public void setJti(String jti) {
    this.jti = jti;
  }

  public Map<String, Object> getJwtToken() {
    return jwtToken;
  }

  public void setJwtToken(Map<String, Object> jwtToken) {
    this.jwtToken = jwtToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
