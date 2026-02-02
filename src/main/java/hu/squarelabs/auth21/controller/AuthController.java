package hu.squarelabs.auth21.controller;

import hu.squarelabs.auth21.model.dto.request.LoginRequest;
import hu.squarelabs.auth21.model.dto.request.RefreshRequest;
import hu.squarelabs.auth21.model.dto.request.RegistrationRequest;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest requestBody) {
    return authService.login(requestBody.email(), requestBody.password());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegistrationRequest requestBody) {
    final String newUserId = authService.register(requestBody);
    return ResponseEntity.created(URI.create("/api/v1/users/" + newUserId)).build();
  }

  @PostMapping("/refresh")
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest requestBody)
      throws ResponseStatusException {
    return authService.refresh(requestBody.refreshToken());
  }

  @GetMapping("/logout")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader("Authorization") String authorizationHeader)
      throws ResponseStatusException {
    authService.logout(authorizationHeader.replace("Bearer ", ""));
  }
}
