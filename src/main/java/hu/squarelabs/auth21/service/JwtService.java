package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration:3600}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration:86400}")
  private long refreshExpiration;

  public String extractUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String userId = extractUserId(token);
    return (userId.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public JwtToken createJwtToken(UserEntity user) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(jwtExpiration);

    Map<String, Object> userData = new HashMap<>();
    userData.put("id", user.getId());
    userData.put("email", user.getEmail());
    userData.put("nickname", user.getDisplayName());

    JwtToken jwtToken = new JwtToken();
    jwtToken.setJti(java.util.UUID.randomUUID().toString());
    jwtToken.setSub(user.getId());
    jwtToken.setIat(now.getEpochSecond());
    jwtToken.setExp(exp.getEpochSecond());
    jwtToken.setUser(userData);

    return jwtToken;
  }

  public JwtToken decodeJwtToken(String token) {
    Claims claims = extractAllClaims(token);

    JwtToken jwtToken = new JwtToken();
    jwtToken.setJti(claims.getId());
    jwtToken.setSub(claims.getSubject());
    jwtToken.setIat(claims.getIssuedAt().toInstant().getEpochSecond());
    jwtToken.setExp(claims.getExpiration().toInstant().getEpochSecond());
    jwtToken.setUser(claims.get("user", Map.class));

    return jwtToken;
  }

  public String encodeJwtToken(JwtToken jwtToken) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("jti", jwtToken.getJti());
    claims.put("iat", Date.from(Instant.ofEpochSecond(jwtToken.getIat())));
    claims.put("user", jwtToken.getUser());

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(jwtToken.getSub())
        .setIssuedAt(Date.from(Instant.ofEpochSecond(jwtToken.getIat())))
        .setExpiration(Date.from(Instant.ofEpochSecond(jwtToken.getExp())))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }
}
