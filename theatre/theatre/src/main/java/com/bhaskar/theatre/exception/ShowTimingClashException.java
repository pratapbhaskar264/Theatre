package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class ShowTimingClashException extends CustomException{

    public ShowTimingClashException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
