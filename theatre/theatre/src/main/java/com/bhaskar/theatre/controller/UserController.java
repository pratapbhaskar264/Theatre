package com.bhaskar.theatre.controller;

import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.dto.UserRequestDto;
import com.bhaskar.theatre.dto.UserResponseDto;
import com.bhaskar.theatre.entity.User;
import com.bhaskar.theatre.enums.Role;
import com.bhaskar.theatre.exception.UserExistsException;
import com.bhaskar.theatre.exception.UsernameNotFoundException;
import com.bhaskar.theatre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bhaskar.theatre.constant.ExceptionMessages.USER_EXISTS;
import static com.bhaskar.theatre.constant.ExceptionMessages.USER_NOT_FOUND;

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


    @Secured({"ROLE_SUPER_ADMIN"})
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDto> getAllUsers() {
        List<UserResponseDto> userResponseDtos = userRepository.findAll()
                .stream()
                .map(user -> UserResponseDto.builder()
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .username(user.getUsername())
                        .id(user.getId())
                        .build()
                )
                .toList();
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .data(userResponseDtos)
                        .message("Fetched all users")
                        .build()
        );
    }

    @Secured({"ROLE_SUPER_ADMIN"})
    @PostMapping("/user/promote/{username}")
    public ResponseEntity<UserResponseDto> promoteUserToAdmin(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(userInDb -> {
                    userInDb.setRole(Role.ROLE_ADMIN);
                    return userRepository.save(userInDb);
                })
                .map(updatedUser -> ResponseEntity.ok(UserResponseDto.builder()
                        .email(updatedUser.getEmail())
                        .firstName(updatedUser.getFirstName())
                        .lastName(updatedUser.getLastName())
                        .role(updatedUser.getRole())
                        .username(updatedUser.getUsername())
                        .id(updatedUser.getId())
                        .build())
                )
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponseDto> createUser( @RequestBody UserRequestDto request) {

        if (userRepository.findByUsername(request.getUsername())) {
            throw new UserExistsException(USER_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(
                UserResponseDto.builder()
                        .id(savedUser.getId())
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .firstName(savedUser.getFirstName())
                        .lastName(savedUser.getLastName())
                        .role(savedUser.getRole())
                        .build()
        );
    }




}
