package com.server.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {
  @NotNull
  @Size(min = 10, max = 10, message = "Number of digits must be 10.")
  private String mobileNumber;
}
