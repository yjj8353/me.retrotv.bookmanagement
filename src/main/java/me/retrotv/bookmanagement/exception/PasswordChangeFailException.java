package me.retrotv.bookmanagement.exception;

public class PasswordChangeFailException extends RuntimeException {
    
    public PasswordChangeFailException(String msg) {
        super(msg);
    }

    public PasswordChangeFailException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
