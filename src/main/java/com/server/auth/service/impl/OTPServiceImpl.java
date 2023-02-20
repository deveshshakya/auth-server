package com.server.auth.service.impl;

import com.server.auth.dto.request.OtpRequest;
import com.server.auth.dto.request.OtpSubmitRequest;
import com.server.auth.dto.response.Response;
import com.server.auth.service.OTPService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static com.server.auth.constants.ResponseConstants.FAILED;
import static com.server.auth.constants.ResponseConstants.SUCCESS;

@Service
public class OTPServiceImpl implements OTPService {

  private final Logger logger = LoggerFactory.getLogger(OTPServiceImpl.class);

  private final ValueOperations<String, String> stringValueOperations;

  @Autowired
  OTPServiceImpl(
      @Qualifier(value = "redisForString") RedisTemplate<String, String> redisForString) {
    this.stringValueOperations = redisForString.opsForValue();
  }

  @Override
  public Response generateOtp(OtpRequest otpRequest) {
    String mobile = otpRequest.getMobileNumber();
    Map<String, Boolean> smsSentResult = null;

    String alreadyGeneratedOtp = findOTPByMobile(mobile);
    if (!StringUtils.isEmpty(alreadyGeneratedOtp)) {
      smsSentResult = sendSMS(mobile, alreadyGeneratedOtp);
    } else {
      smsSentResult = sendSMS(mobile, generateNewOtp(mobile));
    }
    if (smsSentResult.get("success")) {
      return new Response(SUCCESS, "OTP sent successfully.", null);
    }
    return new Response(FAILED, "OTP sending failed.", null);
  }

  @Override
  public Boolean verifyOtp(OtpSubmitRequest otpSubmitRequest) {
    String otp = findOTPByMobile(otpSubmitRequest.getMobileNumber());
    if (StringUtils.isEmpty(otp)) {
      return false;
    }
    if (otp.equals(otpSubmitRequest.getOtp())) {
      removeOtpFromCache(otpSubmitRequest.getMobileNumber());
      return true;
    }
    return false;
  }

  private Map<String, Boolean> sendSMS(String mobile, String otp) {
    logger.debug("OTP: " + otp);
    return Collections.singletonMap("success", true);
  }

  private void removeOtpFromCache(String mobileNumber) {
    // Removing generated OTP
    stringValueOperations.set(getOtpByMobileKey(mobileNumber), "", Duration.ofMillis(1));
  }

  private String generateNewOtp(String mobile) {
    String otp = getRandomOtp();
    stringValueOperations.set(getOtpByMobileKey(mobile), otp, Duration.ofDays(1));
    return otp;
  }

  private String findOTPByMobile(String mobile) {
    return stringValueOperations.get(getOtpByMobileKey(mobile));
  }

  private String getRandomOtp() {
    return String.valueOf((int) ((Math.random() * 9000) + 1000));
  }

  private String getOtpByMobileKey(String mobileNumber) {
    String OTP_BY_MOBILE = "otpByMobile";
    return OTP_BY_MOBILE + "_" + mobileNumber;
  }
}
