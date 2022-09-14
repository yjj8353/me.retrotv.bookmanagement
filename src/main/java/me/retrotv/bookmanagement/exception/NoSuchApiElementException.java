package me.retrotv.bookmanagement.exception;

public class NoSuchApiElementException extends RuntimeException {

    public NoSuchApiElementException(String msg) {
        super(msg);
    }

    public NoSuchApiElementException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
