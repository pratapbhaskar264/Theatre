package com.bhaskar.theatre.controller;


import com.bhaskar.theatre.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException customException){
        return ResponseEntity
                .status(customException.getHttpStatus())
                .body(Map.of("message", customException.getMessage()));
    }
}