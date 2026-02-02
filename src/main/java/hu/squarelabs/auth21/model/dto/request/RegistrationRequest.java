package hu.squarelabs.auth21.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String username,
    String displayName) {}
