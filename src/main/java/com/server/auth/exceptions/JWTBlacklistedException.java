package com.server.auth.exceptions;

import java.io.Serial;

public class JWTBlacklistedException extends Exception {
  @Serial private static final long serialVersionUID = 197773891666193L;

  public JWTBlacklistedException(String message) {
    super(String.format(message));
  }
}
