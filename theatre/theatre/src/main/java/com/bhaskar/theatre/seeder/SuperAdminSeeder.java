package com.bhaskar.theatre.seeder;

import com.bhaskar.theatre.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminSeeder {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SuperAdminSeeder(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
}
