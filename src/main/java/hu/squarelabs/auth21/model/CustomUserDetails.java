package hu.squarelabs.auth21.model;

import hu.squarelabs.auth21.model.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(UserEntity userEntity) implements UserDetails {

  @NonNull
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
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
