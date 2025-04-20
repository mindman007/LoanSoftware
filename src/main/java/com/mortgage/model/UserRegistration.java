package com.mortgage.model;

import java.util.Date;

public class UserRegistration {
    private String username;
    private String email;

    public UserRegistration(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
}