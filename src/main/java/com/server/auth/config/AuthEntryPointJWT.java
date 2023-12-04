package com.server.auth.config;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class AuthEntryPointJWT implements AuthenticationEntryPoint {
  private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJWT.class);

  /** This method will be triggered when any exception is thrown during authentication. */
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    logger.error("Unauthorized error: {}", authException.getMessage());

    // Setting customized error messages.
    final Map<String, Object> body = new HashMap<>();

    final String beforeFilterMessage = (String) request.getAttribute("beforeFilter");
    final String expiredMsg = (String) request.getAttribute("jwtException");
    final String message = !StringUtils.isEmpty(expiredMsg) ? expiredMsg : "Unauthorized";

    if ("invalidSource".equals(beforeFilterMessage)) {
      body.put("status", UNAUTHORIZED);
      body.put("message", "Invalid source.");
    } else if ("tooManyRequests".equals(beforeFilterMessage)) {
      body.put("status", HttpStatus.TOO_MANY_REQUESTS);
      body.put("message", "Too many requests");
    } else {
      body.put("message", message);
      body.put("status", UNAUTHORIZED);
    }

    response.setContentType(APPLICATION_JSON_VALUE);
    response.setStatus(UNAUTHORIZED.value());

    final ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), body);
  }
}
