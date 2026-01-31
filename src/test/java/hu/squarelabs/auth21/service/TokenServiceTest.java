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
  @DisplayName("create method")
  class CreateMethod {

    @Test
    @DisplayName("should save token entity with JWT token data")
    void shouldSaveTokenEntityWithJwtTokenData() {
      final JwtToken jwtToken = new JwtToken();
      jwtToken.setJti("test-jti-123");
      jwtToken.setSub("user-123");
      jwtToken.setIat(1000L);
      jwtToken.setExp(4600L);

      final Map<String, Object> userData =
          Map.of(
              "id", "user-123",
              "email", "test@example.com");
      jwtToken.setUser(userData);

      final String refreshToken = "refresh-token-abc";

      tokenService.create(jwtToken, refreshToken);

      verify(tokenRepository, times(1)).save(any(TokenEntity.class));
    }

    @Test
    @DisplayName("should set expiration time correctly from JWT token")
    void shouldSetExpirationTimeCorrectly() {
      final JwtToken jwtToken = new JwtToken();
      jwtToken.setJti("test-jti-123");
      jwtToken.setSub("user-123");
      jwtToken.setIat(1000L);
      jwtToken.setExp(5000L);
      jwtToken.setUser(Map.of("id", "user-123"));

      final String refreshToken = "refresh-token-abc";

      tokenService.create(jwtToken, refreshToken);

      verify(tokenRepository)
          .save(argThat(entity -> entity.getExpiresAt().getEpochSecond() == 5000L));
    }

    @Test
    @DisplayName("should set user ID from JWT token user data")
    void shouldSetUserIdFromJwtTokenUserData() {
      final JwtToken jwtToken = new JwtToken();
      jwtToken.setJti("test-jti-123");
      jwtToken.setSub("subject");
      jwtToken.setIat(1000L);
      jwtToken.setExp(5000L);
      jwtToken.setUser(Map.of("id", "user-456"));

      final String refreshToken = "refresh-token-abc";

      tokenService.create(jwtToken, refreshToken);

      verify(tokenRepository).save(argThat(entity -> entity.getUserId().equals("user-456")));
    }

    @Test
    @DisplayName("should set user ID from subject when user data is missing")
    void shouldSetUserIdFromSubjectWhenUserDataMissing() {

      final JwtToken jwtToken = new JwtToken();
      jwtToken.setJti("test-jti-123");
      jwtToken.setSub("subject-user");
      jwtToken.setIat(1000L);
      jwtToken.setExp(5000L);
      jwtToken.setUser(null);

      final String refreshToken = "refresh-token-abc";

      tokenService.create(jwtToken, refreshToken);

      verify(tokenRepository).save(argThat(entity -> entity.getUserId().equals("subject-user")));
    }
  }

  @Nested
  @DisplayName("deleteById method")
  class DeleteByIdMethod {

    @Test
    @DisplayName("should call repository deleteById with correct JTI")
    void shouldCallRepositoryDeleteByIdWithCorrectJti() throws Exception {

      final String jti = "token-jti-123";

      tokenService.deleteById(jti);

      verify(tokenRepository, times(1)).deleteById("token-jti-123");
    }
  }

  @Nested
  @DisplayName("getById method")
  class GetByIdMethod {

    @Test
    @DisplayName("should return token data when token exists")
    void shouldReturnTokenDataWhenTokenExists() throws Exception {

      final String jti = "token-jti-123";
      final TokenEntity tokenEntity = new TokenEntity();
      tokenEntity.setJti(jti);
      final Map<String, Object> jwtTokenMap = Map.of("jti", jti, "sub", "user-123");
      tokenEntity.setJwtToken(jwtTokenMap);
      tokenEntity.setRefreshToken("refresh-123");

      when(tokenRepository.findById(jti)).thenReturn(Optional.of(tokenEntity));

      final Optional<Map<String, Object>> result = tokenService.getById(jti);

      assertThat(result).isPresent();
      assertThat(result.get())
          .containsKeys("jwt_token", "refresh_token")
          .containsEntry("jwt_token", jwtTokenMap)
          .containsEntry("refresh_token", "refresh-123");
    }

    @Test
    @DisplayName("should return empty optional when token does not exist")
    void shouldReturnEmptyOptionalWhenTokenNotFound() throws Exception {

      String jti = "non-existent-jti";
      when(tokenRepository.findById(jti)).thenReturn(Optional.empty());

      Optional<Map<String, Object>> result = tokenService.getById(jti);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getByRefreshToken method")
  class GetByRefreshTokenMethod {

    @Test
    @DisplayName("should return token data when refresh token exists")
    void shouldReturnTokenDataWhenRefreshTokenExists() throws Exception {

      String refreshToken = "refresh-abc-123";
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
      tokenEntity.setRefreshToken(refreshToken);

      when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(tokenEntity));

      var result = tokenService.getByRefreshToken(refreshToken);

      assertThat(result).isPresent();
      var pair = result.get();
      assertThat(pair.getLeft()).isNotNull();
      assertThat(pair.getLeft().getJti()).isEqualTo("jti-123");
      assertThat(pair.getRight()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("should return empty optional when refresh token does not exist")
    void shouldReturnEmptyOptionalWhenRefreshTokenNotFound() throws Exception {

      String refreshToken = "non-existent-refresh";
      when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

      var result = tokenService.getByRefreshToken(refreshToken);

      assertThat(result).isEmpty();
    }
  }
}
