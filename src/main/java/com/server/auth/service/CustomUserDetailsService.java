package com.server.auth.service;

import com.server.auth.model.PrincipleUser;
import com.server.auth.model.User;
import com.server.auth.repository.UserRepo;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepo userRepo;

  @Autowired
  public CustomUserDetailsService(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
    User user = userRepo.findByMobileNumber(mobileNumber);
    if (Objects.isNull(user)) {
      throw new UsernameNotFoundException("User not found.");
    }

    List<GrantedAuthority> roles =
        user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    // Passing password as empty string
    return new PrincipleUser(
        user.getMobileNumber(),
        "",
        true,
        true,
        true,
        true,
        roles,
        user.getId(),
        user.getIsActive());
  }
}
