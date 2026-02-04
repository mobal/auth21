package hu.squarelabs.auth21.exception;

import org.springframework.web.server.ResponseStatusException;

public class UserAlreadyExistsException extends ResponseStatusException {
  public UserAlreadyExistsException(String email, String username) {
    super(
        org.springframework.http.HttpStatus.CONFLICT,
        String.format("User with email '%s' or username '%s' already exists.", email, username));
  }
}
