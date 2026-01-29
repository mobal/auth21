package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.entity.UserEntity;
import hu.squarelabs.auth21.repository.TokenRepository;
import hu.squarelabs.auth21.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;

  private static final Logger logger = LogManager.getLogger(AuthService.class);

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.token.lifetime:3600}")
  private int jwtTokenLifetime;

  private static final String ERROR_MESSAGE_INTERNAL_SERVER_ERROR = "Internal Server Error";
  private static final String ERROR_MESSAGE_TOKEN_NOT_FOUND = "The requested token was not found";
  private static final String ERROR_MESSAGE_UNAUTHORIZED = "Unauthorized";
  private static final String ERROR_MESSAGE_USER_NOT_FOUND = "The requested user was not found";

  public AuthService(
      UserRepository userRepository,
      TokenRepository tokenRepository,
      TokenService tokenService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.tokenService = tokenService;
    this.passwordEncoder = passwordEncoder;
  }

  private JwtToken generateToken(String sub, Integer exp, UserEntity user) {
    final var iat = Instant.now();
    final Instant expTime;

    if (exp == null) {
      expTime = iat.plusSeconds(jwtTokenLifetime);
    } else {
      expTime = iat.plusSeconds(exp);
    }

    final var jwtToken = new JwtToken();
    jwtToken.setJti(UUID.randomUUID().toString());
    jwtToken.setSub(sub);
    jwtToken.setIat(iat.getEpochSecond());
    jwtToken.setExp(expTime.getEpochSecond());

    if (user != null) {
      final var userData =
          Map.of(
              "id", user.getId(),
              "email", user.getEmail(),
              "nickname", user.getNickname(),
              "name", user.getName(),
              "roles", user.getRoles());
      jwtToken.setUser(userData);
    }

    return jwtToken;
  }

  private String generateRefreshToken(int length) {
    final var bytes = new byte[length / 2];
    new java.security.SecureRandom().nextBytes(bytes);

    final var sb = new StringBuilder();
    for (final byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private Map.Entry<JwtToken, String> generateTokensForUser(JwtToken jwtToken) {
    final var newJwtToken = generateToken(jwtToken.getSub(), jwtTokenLifetime, null);
    final var refreshToken = generateRefreshToken(16);

    tokenService.create(newJwtToken, refreshToken);

    return Map.entry(newJwtToken, refreshToken);
  }

  private void revokeToken(JwtToken jwtToken) {
    tokenService.deleteById(jwtToken.getJti());
  }

  public Map.Entry<String, String> login(String email, String password) {
    final var userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_MESSAGE_USER_NOT_FOUND);
    }

    final var user = userOpt.get();

    final var passwordValid = verifyPassword(password, user.getPasswordHash());

    if (!passwordValid) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE_UNAUTHORIZED);
    }

    final var jwtToken = generateToken(user.getId(), null, user);
    final var refreshToken = generateRefreshToken(16);
    tokenService.create(jwtToken, refreshToken);

    return Map.entry(encodeJwt(jwtToken), refreshToken);
  }

  public void logout(JwtToken jwtToken) {
    tokenService.deleteById(jwtToken.getJti());
  }

  public Map.Entry<String, String> refresh(JwtToken jwtToken, String refreshToken) {
    final var itemOpt = tokenService.getByRefreshToken(refreshToken);
    if (itemOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ERROR_MESSAGE_TOKEN_NOT_FOUND);
    }

    final var item = itemOpt.get();
    final var storedToken = (JwtToken) item.get("jwt_token");

    if (!jwtToken.equals(storedToken)) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE_INTERNAL_SERVER_ERROR);
    }

    revokeToken(jwtToken);
    final var newTokens = generateTokensForUser(jwtToken);

    return Map.entry(encodeJwt(newTokens.getKey()), newTokens.getValue());
  }

  private boolean verifyPassword(String password, String passwordHash) {
    return passwordEncoder.matches(password, passwordHash);
  }

  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  private String encodeJwt(JwtToken jwtToken) {
    return "";
  }
}
