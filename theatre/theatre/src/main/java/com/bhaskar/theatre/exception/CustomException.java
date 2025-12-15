package com.bhaskar.theatre.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class CustomException extends RuntimeException{

    HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus ) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
