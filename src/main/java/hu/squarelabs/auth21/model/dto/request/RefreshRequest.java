package hu.squarelabs.auth21.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshRequest(@JsonProperty("refresh_token") String refreshToken) {}
