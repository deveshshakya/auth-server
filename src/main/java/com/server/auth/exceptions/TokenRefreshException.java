package com.server.auth.exceptions;

import java.io.Serial;

public class TokenRefreshException extends RuntimeException {
  @Serial private static final long serialVersionUID = 198193891938193L;

  public TokenRefreshException(String message) {
    super(String.format("Failed for %s", message));
  }
}
