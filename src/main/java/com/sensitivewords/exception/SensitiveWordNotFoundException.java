package com.sensitivewords.exception;

public class SensitiveWordNotFoundException extends RuntimeException {

    public SensitiveWordNotFoundException(Long id) {
        super("Sensitive word with id " + id + " not found");
    }
}
