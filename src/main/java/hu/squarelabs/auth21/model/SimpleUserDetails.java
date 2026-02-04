package hu.squarelabs.auth21.model;

import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.Collection;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record SimpleUserDetails(UserEntity userEntity) implements UserDetails {

  @NonNull
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    var roles = userEntity.getRoles();
    if (roles == null || roles.isEmpty()) {
      return java.util.Collections.emptyList();
    }
    return roles.stream()
        .filter(role -> role != null && !role.isBlank())
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .toList();
  }

  @NonNull
  public String getUserId() {
    return userEntity.getId();
  }

  @Override
  public String getPassword() {
    return userEntity.getPassword();
  }

  @NonNull
  @Override
  public String getUsername() {
    return userEntity.getUsername();
  }

  @Override
  public boolean isEnabled() {
    return userEntity.getDeletedAt() == null;
  }
}
