package hu.squarelabs.auth21.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Long expiresIn) {

  @JsonProperty("token_type")
  public String tokenType() {
    return "Bearer";
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
        + tokenType()
        + '\''
        + ", expiresIn="
        + expiresIn
        + '}';
  }
}
