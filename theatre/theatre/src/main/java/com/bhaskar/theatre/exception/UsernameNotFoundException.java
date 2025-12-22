package com.bhaskar.theatre.exception;

import org.springframework.http.HttpStatus;

public class UsernameNotFoundException extends CustomException{
    public UsernameNotFoundException(String message , HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
