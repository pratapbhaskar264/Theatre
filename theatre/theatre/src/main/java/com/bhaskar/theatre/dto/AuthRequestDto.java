package com.bhaskar.theatre.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRequestDto {
    String username;
    String password;
}
