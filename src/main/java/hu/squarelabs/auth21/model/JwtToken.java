package hu.squarelabs.auth21.model;

import java.util.Map;
import java.util.Objects;

public class JwtToken {
  private String jti;
  private String sub;
  private Long iat;
  private Long exp;
  private Map<String, Object> user;

  public JwtToken() {}

  public JwtToken(String jti, String sub, Long iat, Long exp, Map<String, Object> user) {
    this.jti = jti;
    this.sub = sub;
    this.iat = iat;
    this.exp = exp;
    this.user = user;
  }

  public String getJti() {
    return jti;
  }

  public void setJti(String jti) {
    this.jti = jti;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public Long getIat() {
    return iat;
  }

  public void setIat(Long iat) {
    this.iat = iat;
  }

  public Long getExp() {
    return exp;
  }

  public void setExp(Long exp) {
    this.exp = exp;
  }

  public Map<String, Object> getUser() {
    return user;
  }

  public void setUser(Map<String, Object> user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JwtToken jwtToken = (JwtToken) o;
    return Objects.equals(jti, jwtToken.jti)
        && Objects.equals(sub, jwtToken.sub)
        && Objects.equals(iat, jwtToken.iat)
        && Objects.equals(exp, jwtToken.exp)
        && Objects.equals(user, jwtToken.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jti, sub, iat, exp, user);
  }

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
