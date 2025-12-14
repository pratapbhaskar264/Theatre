package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{
    String message;
    HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus ) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
