package me.retrotv.bookmanagement.exception;

public class XmlParseErrorException extends RuntimeException {

    public XmlParseErrorException(String msg) {
        super(msg);
    }

    public XmlParseErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
