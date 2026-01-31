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
    tokenEntity.setJti(jwtToken.getJti());

    final Map<String, Object> jwtTokenMap = new HashMap<>();
    jwtTokenMap.put("jti", jwtToken.getJti());
    jwtTokenMap.put("sub", jwtToken.getSub());
    jwtTokenMap.put("iat", jwtToken.getIat());
    jwtTokenMap.put("exp", jwtToken.getExp());
    jwtTokenMap.put("user", jwtToken.getUser());

    tokenEntity.setJwtToken(jwtTokenMap);
    tokenEntity.setRefreshToken(refreshToken);
    tokenEntity.setCreatedAt(Instant.now());
    tokenEntity.setUpdatedAt(Instant.now());
    tokenEntity.setExpiresAt(Instant.ofEpochSecond(jwtToken.getExp()));

    if (jwtToken.getUser() != null && jwtToken.getUser().containsKey("id")) {
      tokenEntity.setUserId(jwtToken.getUser().get("id").toString());
    } else {
      tokenEntity.setUserId(jwtToken.getSub());
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
              final JwtToken jwtToken = new JwtToken();
              jwtToken.setJti((String) entity.getJwtToken().get("jti"));
              jwtToken.setSub((String) entity.getJwtToken().get("sub"));
              jwtToken.setIat(((Number) entity.getJwtToken().get("iat")).longValue());
              jwtToken.setExp(((Number) entity.getJwtToken().get("exp")).longValue());
              jwtToken.setUser((Map<String, Object>) entity.getJwtToken().get("user"));
              return ImmutablePair.of(jwtToken, entity.getRefreshToken());
            });
  }
}
