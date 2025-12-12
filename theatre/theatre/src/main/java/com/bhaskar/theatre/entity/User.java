package com.bhaskar.theatre.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name ="users" , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

    @Column(nullable = false,length = 50)
     private String username;

    @Column(nullable = false,columnDefinition = "text")
     private String password;

    @Column(length = 50)
     private String firstName;

    @Column(length = 50)
     private String lastName;

    @Column(length = 50)
     private String country;

    @Enumerated(EnumType.STRING)
     private Movie role;



}
