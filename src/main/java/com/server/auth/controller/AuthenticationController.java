package com.server.auth.controller;

import com.server.auth.dto.request.LogoutRequest;
import com.server.auth.dto.request.OtpRequest;
import com.server.auth.dto.request.OtpSubmitRequest;
import com.server.auth.dto.request.TokenRefreshRequest;
import com.server.auth.dto.response.Response;
import com.server.auth.exceptions.TokenRefreshException;
import com.server.auth.model.RefreshToken;
import com.server.auth.service.AuthenticationService;
import com.server.auth.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.server.auth.constants.ResponseConstants.FAILED;
import static com.server.auth.constants.ResponseConstants.SUCCESS;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

  private final AuthenticationService authenticationService;

  @Autowired
  AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/otp")
  public ResponseEntity<Response> getOtp(@RequestBody @Valid OtpRequest otpRequest) {
    try {
      return new ResponseEntity<>(authenticationService.generateOtp(otpRequest), HttpStatus.OK);
    } catch (Exception exception) {
      String stackTrace = ExceptionUtils.getStackTrace(exception);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "OTP generation failed.", null), INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/verifyOtp")
  public ResponseEntity<Response> verifyOtp(@RequestBody @Valid OtpSubmitRequest otpSubmitRequest) {
    try {
      return new ResponseEntity<>(authenticationService.verifyOtp(otpSubmitRequest), HttpStatus.OK);
    } catch (ExpiredJwtException jwtException) {
      String stackTrace = ExceptionUtils.getStackTrace(jwtException);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "Token expired, ask for refresh token.", null), UNAUTHORIZED);
    } catch (Exception exception) {
      String stackTrace = ExceptionUtils.getStackTrace(exception);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "OTP verification failed.", null), INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/refreshToken")
  public ResponseEntity<Response> refreshToken(
      @RequestBody @Valid TokenRefreshRequest tokenRefreshRequest) {
    try {
      String token = tokenRefreshRequest.getRefreshToken();
      RefreshToken refreshToken = authenticationService.verifyRefreshToken(token);
      return new ResponseEntity<>(
          new Response(
              SUCCESS,
              "Access token generated.",
              authenticationService.generateTokenAfterExpire(
                  refreshToken.getMobileNumber(), refreshToken.getRefreshToken())),
          OK);
    } catch (TokenRefreshException tokenRefreshException) {
      return new ResponseEntity<>(
          new Response(FAILED, tokenRefreshException.getMessage(), null), UNAUTHORIZED);
    } catch (Exception exception) {
      String stackTrace = ExceptionUtils.getStackTrace(exception);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "Refresh token generation failed.", null), INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Response> logout(
      @RequestBody @Valid LogoutRequest logoutRequest, HttpServletRequest request) {
    try {
      String jwt = JWTService.jwtFromRequest(request);
      return new ResponseEntity<>(authenticationService.logout(logoutRequest, jwt), OK);
    } catch (TokenRefreshException tokenRefreshException) {
      return new ResponseEntity<>(
          new Response(FAILED, tokenRefreshException.getMessage(), null), UNAUTHORIZED);
    } catch (Exception exception) {
      String stackTrace = ExceptionUtils.getStackTrace(exception);
      logger.error("Error occurred: " + stackTrace);
      return new ResponseEntity<>(
          new Response(FAILED, "Logout failed.", null), INTERNAL_SERVER_ERROR);
    }
  }
}
