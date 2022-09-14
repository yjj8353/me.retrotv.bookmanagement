package me.retrotv.bookmanagement.exception;

public class CertifyFailException extends RuntimeException {
    
    public CertifyFailException(String msg) {
        super(msg);
    }

    public CertifyFailException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
