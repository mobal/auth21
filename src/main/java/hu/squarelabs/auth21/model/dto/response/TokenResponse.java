package hu.squarelabs.auth21.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

  @JsonProperty("access_token")
  private final String accessToken;

  @JsonProperty("refresh_token")
  private final String refreshToken;

  @JsonProperty("expires_in")
  private final Long expiresIn;

  @JsonProperty("token_type")
  private final String tokenType = "Bearer";

  public TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  @Override
  public String toString() {
    return "TokenResponse{"
        + "accessToken='"
        + (accessToken != null ? "[PROTECTED]" : null)
        + '\''
        + ", refreshToken='"
        + (refreshToken != null ? "[PROTECTED]" : null)
        + '\''
        + ", tokenType='"
        + tokenType
        + '\''
        + ", expiresIn="
        + expiresIn
        + '}';
  }
}
