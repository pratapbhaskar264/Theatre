package com.bhaskar.theatre.dto;


import com.bhaskar.theatre.enums.Role;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserResponseDto {

    private long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Role role;
}
