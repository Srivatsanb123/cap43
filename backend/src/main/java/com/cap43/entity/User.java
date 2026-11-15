package com.cap43.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String email;
    private String passwordHash;
    private String role;
    private String fullName;
    private String phone;
    private boolean enabled;
    private LocalDateTime lastLoginAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
