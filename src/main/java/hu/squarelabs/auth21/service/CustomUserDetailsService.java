package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.CustomUserDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserService userService;

  public CustomUserDetailsService(UserService userService) {
    this.userService = userService;
  }

  @NonNull
  @Override
  public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
    return userService
        .getUserByUserName(username)
        .or(() -> userService.getUserByEmail(username))
        .map(CustomUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
