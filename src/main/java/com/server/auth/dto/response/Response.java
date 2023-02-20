package com.server.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response implements Serializable {

  @Serial private static final long serialVersionUID = 198193891000193L;

  private String status;
  private String message;
  private Object result;
}
