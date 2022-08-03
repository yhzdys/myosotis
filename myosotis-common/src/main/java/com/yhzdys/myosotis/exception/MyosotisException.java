package com.yhzdys.myosotis.exception;

public final class MyosotisException extends RuntimeException {

    public MyosotisException(String message) {
        super(message);
    }

    public MyosotisException(Exception e) {
        super(e);
    }

    public MyosotisException(String message, Throwable cause) {
        super(message, cause);
    }
}
