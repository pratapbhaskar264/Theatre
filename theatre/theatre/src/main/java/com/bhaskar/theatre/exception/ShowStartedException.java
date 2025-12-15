package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class ShowStartedException extends CustomException{
    public ShowStartedException(String message, HttpStatus httpStatus){
        super(message,httpStatus);
    }
}
