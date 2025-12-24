package com.bhaskar.theatre.service;


import com.bhaskar.theatre.dto.SignupRequestDto;
import com.bhaskar.theatre.entity.User;
import com.bhaskar.theatre.enums.Role;
import com.bhaskar.theatre.exception.UserExistsException;
import com.bhaskar.theatre.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.bhaskar.theatre.constant.ExceptionMessages.USER_EXISTS;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    public String signupUser(SignupRequestDto signupRequestDto) {

        if(userRepository.findByUsername(signupRequestDto.getUsername()).isPresent())
            throw new UserExistsException(USER_EXISTS, HttpStatus.BAD_REQUEST);

        User user = User.builder()
                .username(signupRequestDto.getUsername())
                .firstName(signupRequestDto.getFirstName())
                .lastName(signupRequestDto.getLastName())
                .password(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()))
                .email(signupRequestDto.getEmail())
                .role(signupRequestDto.getRole())
                .build();

        User createdUser = userRepository.save(user);
        return jwtService.generateToken(createdUser);
    }

    public String authenticateUser(String username) {
        return userRepository.findByUsername(username)
                .map(jwtService::generateToken)
                .orElseThrow(RuntimeException::new);

    }
}