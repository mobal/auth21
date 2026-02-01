package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.exception.UserNotFoundException;
import hu.squarelabs.auth21.model.CustomUserDetails;
import hu.squarelabs.auth21.model.JwtToken;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.model.entity.UserEntity;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private static final SecureRandom secureRandom = new SecureRandom();

  private final UserService userService;
  private final TokenService tokenService;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthService(
      UserService userService,
      TokenService tokenService,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.tokenService = tokenService;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  public TokenResponse login(String email, String password) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    final CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    final UserEntity user = userDetails.userEntity();

    final JwtToken jwtToken = jwtService.createJwtToken(user);
    final String refreshToken = generateRefreshToken();

    tokenService.create(jwtToken, refreshToken);

    return new TokenResponse(jwtService.encodeJwtToken(jwtToken), refreshToken, jwtToken.exp());
  }

  public TokenResponse refresh(String refreshToken) throws ResponseStatusException {
    Optional<Pair<JwtToken, String>> tokens = tokenService.getByRefreshToken(refreshToken);

    if (tokens.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid refresh token");
    }

    Pair<JwtToken, String> data = tokens.get();
    final var jwtToken = data.getLeft();
    final var userId = (String) ((Map<?, ?>) jwtToken.user()).get("id");
    final var user =
        userService
            .getUserById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

    JwtToken newJwtToken = jwtService.createJwtToken(user);
    String newRefreshToken = generateRefreshToken();

    tokenService.deleteById(jwtToken.jti());
    tokenService.create(newJwtToken, newRefreshToken);

    return new TokenResponse(
        jwtService.encodeJwtToken(newJwtToken), newRefreshToken, newJwtToken.exp());
  }

  public void logout(String jwtToken) {
    final JwtToken decodedToken = jwtService.decodeJwtToken(jwtToken);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      SecurityContextHolder.clearContext();

      tokenService.deleteById(decodedToken.jti());
    }
  }

  private String generateRefreshToken() {
    return generateRefreshToken(16);
  }

  private String generateRefreshToken(int length) {
    final var bytes = new byte[length];
    secureRandom.nextBytes(bytes);

    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }
}
