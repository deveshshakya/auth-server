package com.server.auth.exceptions;

import java.io.Serial;

public class InvalidRequestException extends Exception {
  @Serial private static final long serialVersionUID = 197773891938193L;

  public InvalidRequestException(String message) {
    super(String.format(message));
  }
}
