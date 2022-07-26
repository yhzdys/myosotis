package com.yhzdys.myosotis.exception;

/**
 * myosotis exception
 */
public final class MyosotisException extends RuntimeException {

    public MyosotisException(String message) {
        super(message);
    }

    public MyosotisException(Exception e) {
    }

    public MyosotisException(String message, Throwable cause) {
        super(message, cause);
    }
}
