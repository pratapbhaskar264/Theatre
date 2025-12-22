package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class UserExistsException extends CustomException {
    public UserExistsException(String message, HttpStatus badRequest) {
        super(message, HttpStatus.CONFLICT);
    }
}
