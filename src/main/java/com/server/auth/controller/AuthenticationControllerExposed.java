package com.server.auth.controller;

import static com.server.auth.constants.ResponseConstants.FAILED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import com.server.auth.dto.request.TokenExpireRequest;
import com.server.auth.dto.response.Response;
import com.server.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exposed")
public class AuthenticationControllerExposed {

  private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

  private final AuthenticationService authenticationService;

  @Autowired
  AuthenticationControllerExposed(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/invalidateToken")
  public ResponseEntity<Response> invalidateToken(
      @RequestBody @Valid TokenExpireRequest tokenExpireRequest) {
    try {
      return new ResponseEntity<>(authenticationService.invalidateToken(tokenExpireRequest), OK);
    } catch (Exception exception) {
      String stackTrace = ExceptionUtils.getStackTrace(exception);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "Token expiration failed.", null), INTERNAL_SERVER_ERROR);
    }
  }
}
