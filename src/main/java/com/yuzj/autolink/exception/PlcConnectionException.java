package com.yuzj.autolink.exception;

public class PlcConnectionException extends Exception {
    public PlcConnectionException(String message) {
        super(message);
    }
    
    public PlcConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}