package com.server.auth.config;

import com.server.auth.filter.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JWTFilter jwtFilter;
  private final AuthEntryPointJWT authEntryPointJWT;

  @Autowired
  public WebSecurityConfig(
      UserDetailsService userDetailsService,
      JWTFilter jwtFilter,
      AuthEntryPointJWT authEntryPointJWT) {
    this.userDetailsService = userDetailsService;
    this.jwtFilter = jwtFilter;
    this.authEntryPointJWT = authEntryPointJWT;
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(this.userDetailsService);
    authenticationProvider.setPasswordEncoder(this.passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManagerBean(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .cors()
        .and()
        .csrf()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(authEntryPointJWT)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(STATELESS)
        .and()
        .authorizeHttpRequests()
        .requestMatchers("/auth/otp", "/auth/verifyOtp", "/auth/refreshToken", "/exposed/**")
        .permitAll()
        .anyRequest()
        .authenticated();

    httpSecurity.authenticationProvider(this.authenticationProvider());
    httpSecurity.addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }
}
