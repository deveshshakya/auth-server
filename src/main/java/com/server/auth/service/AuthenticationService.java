package com.server.auth.service;

import com.server.auth.dto.request.LogoutRequest;
import com.server.auth.dto.request.OtpRequest;
import com.server.auth.dto.request.OtpSubmitRequest;
import com.server.auth.dto.request.TokenExpireRequest;
import com.server.auth.dto.response.Response;
import com.server.auth.dto.response.TokenRefreshResponse;
import com.server.auth.model.RefreshToken;

public interface AuthenticationService {
  Response generateOtp(OtpRequest otpRequest);

  Response verifyOtp(OtpSubmitRequest otpSubmitRequest);

  RefreshToken verifyRefreshToken(String refreshToken);

  TokenRefreshResponse generateTokenAfterExpire(String mobileNumber, String refreshToken);

  Response logout(LogoutRequest logoutRequest, String jwt);

  Response invalidateToken(TokenExpireRequest tokenExpireRequest);
}
