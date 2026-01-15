package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends  CustomException{
    public UnAuthorizedException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
