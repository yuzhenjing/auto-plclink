package com.yuzj.autolink.exception;

public class PlcReadException extends Exception {
    public PlcReadException(String message) {
        super(message);
    }
    
    public PlcReadException(String message, Throwable cause) {
        super(message, cause);
    }
}