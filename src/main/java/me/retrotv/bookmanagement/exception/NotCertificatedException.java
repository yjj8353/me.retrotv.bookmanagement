package me.retrotv.bookmanagement.exception;

import org.springframework.security.core.AuthenticationException;

public class NotCertificatedException extends AuthenticationException {
    
    public NotCertificatedException(String msg) {
        super(msg);
    }

    public NotCertificatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
