package hu.squarelabs.auth21.controller;

import hu.squarelabs.auth21.model.dto.request.LoginRequest;
import hu.squarelabs.auth21.model.dto.request.RefreshRequest;
import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request.email(), request.password());
  }

  @PostMapping("/refresh")
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest request)
      throws ResponseStatusException {
    return authService.refresh(request.refreshToken());
  }

  @GetMapping("/logout")
  public void logout(@RequestHeader("Authorization") String authorizationHeader)
      throws ResponseStatusException {
    authService.logout(authorizationHeader.replace("Bearer ", ""));
  }
}
