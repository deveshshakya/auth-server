package com.server.auth.model;

import java.io.Serial;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
@Setter
public class PrincipleUser extends User {
  @Serial private static final long serialVersionUID = -3531439484732724601L;

  private final String userId;
  private final Boolean isActive;

  public PrincipleUser(
      String username,
      String password,
      boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities,
      String userId,
      Boolean isActive) {

    super(
        username,
        password,
        enabled,
        accountNonExpired,
        credentialsNonExpired,
        accountNonLocked,
        authorities);

    this.userId = userId;
    this.isActive = isActive;
  }
}
