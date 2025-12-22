package com.bhaskar.theatre.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequestDto {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}
