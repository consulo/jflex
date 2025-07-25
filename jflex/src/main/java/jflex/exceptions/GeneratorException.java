/*
 * Copyright (C) 1998-2018  Gerwin Klein <lsf@jflex.de>
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jflex.exceptions;

/**
 * Thrown when code generation has to be aborted.
 *
 * @author Gerwin Klein
 * @version JFlex 1.10.14
 */
public class GeneratorException extends RuntimeException {

  private static final long serialVersionUID = -9128247888544263982L;
  private boolean unexpected;

  public GeneratorException() {
    super();
    // we use GeneratorException() to abort control flow, i.e. these are
    // expected and we leave unexpected on false.
  }

  public GeneratorException(Throwable cause, boolean unexpected) {
    super("Generation aborted: " + cause, cause);
    this.unexpected = unexpected;
  }

  public GeneratorException(Throwable cause) {
    this(cause, false);
  }

  public boolean isUnExpected() {
    return unexpected;
  }
}
