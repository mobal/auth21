package hu.squarelabs.auth21.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import hu.squarelabs.auth21.model.dto.response.TokenResponse;
import hu.squarelabs.auth21.service.AuthService;

@RestController
@RequestMapping(consumes = { MediaType.ALL_VALUE }, produces = {
        MediaType.APPLICATION_JSON_VALUE }, value = "/api/v1/auth")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseStatus(value = HttpStatus.OK)
    public TokenResponse login() {
        logger.info("Login request received");

        return new TokenResponse(
            "dummy-access-token",
            "dummy-refresh-token",
            3600L
        );
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(value = HttpStatus.OK)
    public TokenResponse refreshToken() {
        logger.info("Refresh token request received");

        return new TokenResponse(
            "dummy-access-token",
            "dummy-refresh-token",
            3600L
        );
    }

    @PostMapping("/register")
    @ResponseStatus(value = HttpStatus.OK)
    public TokenResponse register() {
        logger.info("Register request received");

        return new TokenResponse(
            "dummy-access-token",
            "dummy-refresh-token",
            3600L
        );
    }
}
