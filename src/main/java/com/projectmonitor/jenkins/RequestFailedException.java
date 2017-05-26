package com.projectmonitor.jenkins;

public class RequestFailedException extends Exception {

    public RequestFailedException() {
        super();
    }

    public RequestFailedException(String message) {
        super(message);
    }

    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestFailedException(Throwable cause) {
        super(cause);
    }

    protected RequestFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
