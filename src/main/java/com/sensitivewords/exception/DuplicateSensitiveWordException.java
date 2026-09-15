package com.sensitivewords.exception;

public class DuplicateSensitiveWordException extends RuntimeException {

    public DuplicateSensitiveWordException(String word) {
        super("Sensitive word '" + word + "' already exists");
    }
}
