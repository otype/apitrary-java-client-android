package com.apitrary.sdk;

public class APYException extends Exception {

    private static final long serialVersionUID = -6725646414103116447L;

    public APYException() {}

    public APYException(String message) {
        super(message);
    }

    public APYException(Throwable cause) {
        super(cause);
    }

    public APYException(String message, Throwable cause) {
        super(message, cause);
    }

}
