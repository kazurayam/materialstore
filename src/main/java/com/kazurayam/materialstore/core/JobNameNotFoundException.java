package com.kazurayam.materialstore.core;

public class JobNameNotFoundException extends Exception {

    public JobNameNotFoundException(String message) {
        super(message);
    }

    public JobNameNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public JobNameNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
