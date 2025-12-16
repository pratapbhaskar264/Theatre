package com.bhaskar.theatre.exception;

public class UsernameNotFoundException extends CustomException{
    public UsernameNotFoundException(String message) {
        super(message, httpStatus);
    }
}
