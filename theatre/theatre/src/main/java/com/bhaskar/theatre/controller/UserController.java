package com.bhaskar.theatre.controller;

import com.bhaskar.theatre.dto.ApiResponseDto;
import com.bhaskar.theatre.dto.UserResponseDto;
import com.bhaskar.theatre.enums.Role;
import com.bhaskar.theatre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bhaskar.theatre.constant.ExceptionMessages.USER_NOT_FOUND;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository
                          ) {
        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
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

    @DeleteMapping("/user/me/delete")
    public ResponseEntity<ApiResponseDto> deleteOwnAccount() {
        // Get username from token
        String currentUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(currentUsername)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(
                            ApiResponseDto.builder()
                                    .message("Your account has been deleted successfully")
                                    .build()
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    @Secured({"ROLE_SUPER_ADMIN"})
    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<ApiResponseDto> deleteUserById(@PathVariable long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);

        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .message("User with ID " + userId + " deleted by Admin")
                        .build()
        );
    }
}
