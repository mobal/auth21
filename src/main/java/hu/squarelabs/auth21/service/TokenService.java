package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.entity.TokenEntity;
import hu.squarelabs.auth21.repository.TokenRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private final TokenRepository tokenRepository;

  private static final Logger logger = LogManager.getLogger(TokenService.class);

  public TokenService(TokenRepository tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  public void create(JwtToken jwtToken, String refreshToken) {
    final var tokenEntity = new TokenEntity();
    tokenEntity.setJti(jwtToken.jti());

    final Map<String, Object> jwtTokenMap = new HashMap<>();
    jwtTokenMap.put("jti", jwtToken.jti());
    jwtTokenMap.put("sub", jwtToken.sub());
    jwtTokenMap.put("iat", jwtToken.iat());
    jwtTokenMap.put("exp", jwtToken.exp());
    jwtTokenMap.put("user", jwtToken.user());

    tokenEntity.setJwtToken(jwtTokenMap);
    tokenEntity.setRefreshToken(refreshToken);
    tokenEntity.setCreatedAt(Instant.now());
    tokenEntity.setUpdatedAt(Instant.now());
    tokenEntity.setExpiresAt(Instant.ofEpochSecond(jwtToken.exp()));

    if (jwtToken.user() != null && jwtToken.user().containsKey("id")) {
      tokenEntity.setUserId(jwtToken.user().get("id").toString());
    } else {
      tokenEntity.setUserId(jwtToken.sub());
    }

    tokenRepository.save(tokenEntity);
  }

  public void deleteById(String jti) {
    tokenRepository.deleteById(jti);
  }

  public Optional<Map<String, Object>> getById(String jti) {
    return tokenRepository
        .findById(jti)
        .map(
            entity -> {
              final Map<String, Object> result = new HashMap<>();
              result.put("jwt_token", entity.getJwtToken());
              result.put("refresh_token", entity.getRefreshToken());
              return result;
            });
  }

  public Optional<Pair<JwtToken, String>> getByRefreshToken(String refreshToken) {
    return tokenRepository
        .findByRefreshToken(refreshToken)
        .map(
            entity -> {
              final JwtToken jwtToken =
                  new JwtToken(
                      (String) entity.getJwtToken().get("jti"),
                      (String) entity.getJwtToken().get("sub"),
                      ((Number) entity.getJwtToken().get("iat")).longValue(),
                      ((Number) entity.getJwtToken().get("exp")).longValue(),
                      (Map<String, Object>) entity.getJwtToken().get("user"));
              return ImmutablePair.of(jwtToken, entity.getRefreshToken());
            });
  }
}
