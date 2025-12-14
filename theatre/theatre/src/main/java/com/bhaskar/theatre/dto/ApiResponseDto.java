package com.bhaskar.theatre.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponseDto {
    String message;
    Object data;
}
