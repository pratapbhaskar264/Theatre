package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class SeatLockAccquiredException extends CustomException{

    public SeatLockAccquiredException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
