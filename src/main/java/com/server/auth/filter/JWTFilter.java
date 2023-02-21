package com.server.auth.filter;

import com.server.auth.exceptions.JWTBlacklistedException;
import com.server.auth.service.CustomUserDetailsService;
import com.server.auth.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTFilter extends OncePerRequestFilter {

  private final Logger logger = LoggerFactory.getLogger(JWTFilter.class);
  private final JWTService jwtService;
  private final CustomUserDetailsService customUserDetailsService;

  @Autowired
  public JWTFilter(JWTService jwtService, CustomUserDetailsService customUserDetailsService) {
    this.jwtService = jwtService;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = JWTService.jwtFromRequest(request);
      if (!this.jwtService.checkIfJwtIsBlacklisted(jwt)) {
        String userName = null;
        if (!StringUtils.isEmpty(jwt)) {
          userName = this.jwtService.extractUserName(jwt);
        }
        if (!StringUtils.isEmpty(userName)
            && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
          UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(userName);
          if (this.jwtService.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken);
          }
        }
      } else {
        throw new JWTBlacklistedException("Already logged out.");
      }
    } catch (JWTBlacklistedException | SignatureException | ExpiredJwtException jwtException) {
      request.setAttribute("jwtException", jwtException.getMessage());
    } catch (Exception exception) {
      logger.error(
          "Error occurred while filtering JWT: " + ExceptionUtils.getStackTrace(exception));
    }
    filterChain.doFilter(request, response);
  }
}
