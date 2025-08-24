package com.yuzj.autolink.exception;

public class PlcWriteException extends Exception {
    public PlcWriteException(String message) {
        super(message);
    }
    
    public PlcWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}