package com.example.tools.exception;

public class ProcessException extends RuntimeException {

    public ProcessException(String msg) {
        super(msg);
    }

    public ProcessException(String msg, Exception e) {
        super(msg, e);
    }
}
