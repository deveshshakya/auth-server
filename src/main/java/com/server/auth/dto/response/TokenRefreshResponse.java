package com.server.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
}
