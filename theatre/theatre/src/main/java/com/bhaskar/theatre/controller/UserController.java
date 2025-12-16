package com.bhaskar.theatre.controller;

import com.bhaskar.theatre.dto.UserResponseDto;
import com.bhaskar.theatre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDto> currentUser() {
        String currentUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(currentUsername)
                .map(user -> ResponseEntity.ok(UserResponseDto.builder()
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .username(user.getUsername())
                        .id(user.getId())
                        .build())
                )
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }



}
