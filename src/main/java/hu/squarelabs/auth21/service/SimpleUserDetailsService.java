package hu.squarelabs.auth21.service;

import hu.squarelabs.auth21.model.SimpleUserDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SimpleUserDetailsService implements UserDetailsService {

  private final UserService userService;

  public SimpleUserDetailsService(UserService userService) {
    this.userService = userService;
  }

  @NonNull
  @Override
  public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
    return userService
        .getUserByUserName(username)
        .or(() -> userService.getUserByEmail(username))
        .map(SimpleUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }

  public UserDetails loadUserById(@NonNull String userId) throws UsernameNotFoundException {
    return userService
        .getUserById(userId)
        .map(SimpleUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
  }
}
