package me.retrotv.bookmanagement.exception;

public class EmailSendErrorException extends RuntimeException {
    
    public EmailSendErrorException(String msg) {
        super(msg);
    }

    public EmailSendErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
