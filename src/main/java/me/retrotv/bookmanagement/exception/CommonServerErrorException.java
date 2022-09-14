package me.retrotv.bookmanagement.exception;

public class CommonServerErrorException extends RuntimeException {
    
    public CommonServerErrorException(String msg) {
        super(msg);
    }

    public CommonServerErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
