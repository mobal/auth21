package hu.squarelabs.auth21.model;

import java.util.Map;

public record JwtToken(String jti, String sub, Long iat, Long exp, Map<String, Object> user) {

  @Override
  public String toString() {
    return "JwtToken{"
        + "jti='"
        + jti
        + '\''
        + ", sub='"
        + sub
        + '\''
        + ", iat="
        + iat
        + ", exp="
        + exp
        + ", user="
        + user
        + '}';
  }
}
