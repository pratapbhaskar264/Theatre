package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class ShowNotFoundException extends CustomException {
    public ShowNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
