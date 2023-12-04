package com.server.auth.service.impl;

import static com.server.auth.constants.AuthConstants.TOKEN_TYPE;
import static com.server.auth.constants.ResponseConstants.FAILED;
import static com.server.auth.constants.ResponseConstants.SUCCESS;

import com.server.auth.dto.request.LogoutRequest;
import com.server.auth.dto.request.OtpRequest;
import com.server.auth.dto.request.OtpSubmitRequest;
import com.server.auth.dto.request.TokenExpireRequest;
import com.server.auth.dto.response.JwtResponse;
import com.server.auth.dto.response.Response;
import com.server.auth.dto.response.TokenRefreshResponse;
import com.server.auth.model.PrincipleUser;
import com.server.auth.model.RefreshToken;
import com.server.auth.service.AuthenticationService;
import com.server.auth.service.CustomUserDetailsService;
import com.server.auth.service.JWTService;
import com.server.auth.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private final OTPService otpService;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService customUserDetailsService;
  private final JWTService jwtService;

  @Autowired
  AuthenticationServiceImpl(
      OTPService otpService,
      AuthenticationManager authenticationManager,
      CustomUserDetailsService customUserDetailsService,
      JWTService jwtService) {
    this.otpService = otpService;
    this.authenticationManager = authenticationManager;
    this.customUserDetailsService = customUserDetailsService;
    this.jwtService = jwtService;
  }

  @Override
  public Response generateOtp(OtpRequest otpRequest) {
    return this.otpService.generateOtp(otpRequest);
  }

  @Override
  public Response verifyOtp(OtpSubmitRequest otpSubmitRequest) {
    if (this.otpService.verifyOtp(otpSubmitRequest)) {
      return new Response(SUCCESS, "OTP verified.", createJwt(otpSubmitRequest));
    }
    return new Response(FAILED, "Wrong OTP.", null);
  }

  @Override
  public RefreshToken verifyRefreshToken(String refreshToken) {
    RefreshToken refToken = findRefreshTokenByToken(refreshToken);
    return this.jwtService.verifyExpiration(refToken);
  }

  @Override
  public TokenRefreshResponse generateTokenAfterExpire(String mobileNumber, String refreshToken) {
    PrincipleUser principleUser =
        (PrincipleUser) this.customUserDetailsService.loadUserByUsername(mobileNumber);
    String jwtToken = this.jwtService.generateToken(principleUser);
    String newRefreshToken = this.jwtService.generateRefreshToken(mobileNumber).getRefreshToken();
    return new TokenRefreshResponse(jwtToken, newRefreshToken, TOKEN_TYPE);
  }

  @Override
  public Response logout(LogoutRequest logoutRequest, String jwt) {
    this.jwtService.deleteRefreshToken(logoutRequest.getRefreshToken(), jwt);
    return new Response(SUCCESS, "Logout successful.", null);
  }

  @Override
  public Response invalidateToken(TokenExpireRequest tokenExpireRequest) {
    this.jwtService.invalidateToken(tokenExpireRequest.getToken());
    return new Response(SUCCESS, "Token added to blacklist.", null);
  }

  private RefreshToken findRefreshTokenByToken(String refreshToken) {
    return this.jwtService.findRefreshTokenByToken(refreshToken);
  }

  private JwtResponse createJwt(OtpSubmitRequest otpSubmitRequest) {
    Authentication authentication = null;
    try {
      authentication =
          this.authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(otpSubmitRequest.getMobileNumber(), ""));
    } catch (BadCredentialsException exception) {
      throw new BadCredentialsException("Invalid credentials.");
    }

    PrincipleUser principleUser = (PrincipleUser) authentication.getPrincipal();
    String jwtToken = this.jwtService.generateToken(principleUser);
    String refreshToken =
        this.jwtService.generateRefreshToken(principleUser.getUsername()).getRefreshToken();

    return new JwtResponse(otpSubmitRequest.getMobileNumber(), jwtToken, refreshToken, TOKEN_TYPE);
  }
}
