package com.server.auth.service;

import com.server.auth.dto.request.OtpRequest;
import com.server.auth.dto.request.OtpSubmitRequest;
import com.server.auth.dto.response.Response;

public interface OTPService {
  Response generateOtp(OtpRequest otpRequest);

  Boolean verifyOtp(OtpSubmitRequest otpSubmitRequest);
}
