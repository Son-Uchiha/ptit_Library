package com.ptit.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "username", length = 20)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "is_active")
    private Boolean isActive;

    @Transient
    private String fullName; // From Students table join

    @Transient
    private String gender; // From Students table join

    @Transient
    private LocalDate dateOfBirth; // From Students table join

    @Transient
    private String phone; // From Students table join

    @Transient
    private String address; // From Students table join

    @Transient
    private String major; // From Students table join

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getFirstNameFromFullName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            return parts[parts.length - 1];
        }
        return username;
    }
}
