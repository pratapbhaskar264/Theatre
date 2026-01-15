package com.bhaskar.theatre.controller;


import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.exception.AmountNotMatchException;
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
    @ExceptionHandler(AmountNotMatchException.class)
    public ResponseEntity<ApiResponseDto> handleAmountMismatch(AmountNotMatchException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(
                        ApiResponseDto.builder()
                                .message(ex.getMessage())
                                // We extract the value we saved in the class above
                                .data("Correct amount should be: " + ex.getAmountToBePaid())
                                .build()
                );
    }
}