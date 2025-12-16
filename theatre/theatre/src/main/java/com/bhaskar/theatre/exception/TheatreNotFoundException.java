package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class TheatreNotFoundException extends CustomException {
    public TheatreNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}