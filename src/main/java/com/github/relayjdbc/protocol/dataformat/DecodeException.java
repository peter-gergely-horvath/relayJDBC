package com.github.relayjdbc.protocol.dataformat;

public class DecodeException extends Exception {

    public static final long serialVersionUID = 1;

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
