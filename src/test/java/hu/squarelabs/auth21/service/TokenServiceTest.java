package hu.squarelabs.auth21.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.entity.TokenEntity;
import hu.squarelabs.auth21.repository.TokenRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("TokenService")
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock private TokenRepository tokenRepository;

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService(tokenRepository);
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("saves token entity with JWT token data")
    void savesTokenEntityWithJwtTokenData() {
      JwtToken jwtToken =
          new JwtToken(
              "test-jti-123",
              "user-123",
              1000L,
              4600L,
              Map.of("id", "user-123", "email", "test@example.com"));

      tokenService.create(jwtToken, "refresh-token-abc");

      verify(tokenRepository).save(any(TokenEntity.class));
    }

    @Test
    @DisplayName("sets expiration time from JWT token")
    void setsExpirationTimeFromJwtToken() {
      JwtToken jwtToken =
          new JwtToken("test-jti-123", "user-123", 1000L, 5000L, Map.of("id", "user-123"));

      tokenService.create(jwtToken, "refresh-token-abc");

      verify(tokenRepository)
          .save(argThat(entity -> entity.getExpiresAt().getEpochSecond() == 5000L));
    }

    @Test
    @DisplayName("sets user ID from JWT token user data")
    void setsUserIdFromJwtTokenUserData() {
      JwtToken jwtToken =
          new JwtToken("test-jti-123", "subject", 1000L, 5000L, Map.of("id", "user-456"));

      tokenService.create(jwtToken, "refresh-token-abc");

      verify(tokenRepository).save(argThat(entity -> entity.getUserId().equals("user-456")));
    }

    @Test
    @DisplayName("sets user ID from subject when user data is missing")
    void setsUserIdFromSubjectWhenUserDataIsMissing() {
      JwtToken jwtToken = new JwtToken("test-jti-123", "subject-user", 1000L, 5000L, null);

      tokenService.create(jwtToken, "refresh-token-abc");

      verify(tokenRepository).save(argThat(entity -> entity.getUserId().equals("subject-user")));
    }
  }

  @Nested
  @DisplayName("deleteById")
  class DeleteById {

    @Test
    @DisplayName("deletes token with correct JTI")
    void deletesTokenWithCorrectJti() throws Exception {
      tokenService.deleteById("token-jti-123");

      verify(tokenRepository).deleteById("token-jti-123");
    }
  }

  @Nested
  @DisplayName("getById")
  class GetById {

    @Test
    @DisplayName("returns token data when token exists")
    void returnsTokenDataWhenTokenExists() throws Exception {
      TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("token-jti-123");
      Map<String, Object> jwtTokenMap = Map.of("jti", "token-jti-123", "sub", "user-123");
      tokenEntity.setJwtToken(jwtTokenMap);
      tokenEntity.setRefreshToken("refresh-123");

      when(tokenRepository.findById("token-jti-123")).thenReturn(Optional.of(tokenEntity));

      Optional<Map<String, Object>> result = tokenService.getById("token-jti-123");

      assertThat(result).isPresent();
      assertThat(result.get())
          .containsEntry("jwt_token", jwtTokenMap)
          .containsEntry("refresh_token", "refresh-123");
    }

    @Test
    @DisplayName("returns empty when token does not exist")
    void returnsEmptyWhenTokenDoesNotExist() throws Exception {
      when(tokenRepository.findById("non-existent-jti")).thenReturn(Optional.empty());

      Optional<Map<String, Object>> result = tokenService.getById("non-existent-jti");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getByRefreshToken")
  class GetByRefreshToken {

    @Test
    @DisplayName("returns token data when refresh token exists")
    void returnsTokenDataWhenRefreshTokenExists() throws Exception {
      TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti("jti-123");
      Map<String, Object> jwtTokenMap =
          Map.of(
              "jti",
              "jti-123",
              "sub",
              "user-123",
              "iat",
              1L,
              "exp",
              3601L,
              "user",
              Map.of("id", "user-123"));
      tokenEntity.setJwtToken(jwtTokenMap);
      tokenEntity.setRefreshToken("refresh-abc-123");

      when(tokenRepository.findByRefreshToken("refresh-abc-123"))
          .thenReturn(Optional.of(tokenEntity));

      var result = tokenService.getByRefreshToken("refresh-abc-123");

      assertThat(result).isPresent();
      var pair = result.get();
      assertThat(pair.getLeft().jti()).isEqualTo("jti-123");
      assertThat(pair.getRight()).isEqualTo("refresh-abc-123");
    }

    @Test
    @DisplayName("returns empty when refresh token does not exist")
    void returnsEmptyWhenRefreshTokenDoesNotExist() throws Exception {
      when(tokenRepository.findByRefreshToken("non-existent-refresh")).thenReturn(Optional.empty());

      var result = tokenService.getByRefreshToken("non-existent-refresh");

      assertThat(result).isEmpty();
    }
  }
}
