package com.example.tools.exception;

public class PreparationException extends RuntimeException {

    public PreparationException(Exception e) {
        super(e);
    }

    public PreparationException(String msg) {
        super(msg);
    }

}
