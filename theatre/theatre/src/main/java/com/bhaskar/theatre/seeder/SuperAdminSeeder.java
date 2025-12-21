package com.bhaskar.theatre.seeder;

import com.bhaskar.theatre.entity.User;
import com.bhaskar.theatre.enums.Role;
import com.bhaskar.theatre.repository.UserRepository;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminSeeder {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SuperAdminSeeder(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadSuperAdminUser();
    }

    private void loadSuperAdminUser() {
        User superAdmin = User.builder()
                .role(Role.ROLE_SUPER_ADMIN)
                .username("super_user")
                .password(bCryptPasswordEncoder.encode("super_password"))
                .firstName("super_user_firstname")
                .lastName("super_user_lastname")
                .email("super_user_email")
                .build();

        if(userRepository.findByUsername(superAdmin.getUsername()).isEmpty()){
            userRepository.save(superAdmin);
        }
    }
}
