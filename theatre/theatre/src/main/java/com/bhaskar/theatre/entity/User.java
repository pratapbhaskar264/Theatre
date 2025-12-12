package com.bhaskar.theatre.entity;



import com.bhaskar.theatre.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long id;



//    @Column(nullable = false,columnDefinition = "text")
     private String password;

    @Column(length = 50)
     private String firstName;


    @Column(length = 50)
     private String lastName;

    private String email;
    private String username;
    @Column(length = 50)
     private String country;

    @Enumerated(EnumType.STRING)
     private Role role;


    @Override
    public Collection< ? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
