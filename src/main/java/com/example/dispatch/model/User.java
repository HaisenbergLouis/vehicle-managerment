package com.example.dispatch.model;

import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String role;
    private final LocalDateTime createdAt;

    public User(int id, String username, String passwordHash, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // 注册时使用的构造函数
    public User(String username, String passwordHash, String role) {
        this(0, username, passwordHash, role, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}