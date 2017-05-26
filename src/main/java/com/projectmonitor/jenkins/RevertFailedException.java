package com.projectmonitor.jenkins;

public class RevertFailedException extends Exception {

    public RevertFailedException() {
    }

    public RevertFailedException(String message) {
        super(message);
    }

    public RevertFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RevertFailedException(Throwable cause) {
        super(cause);
    }

    public RevertFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
