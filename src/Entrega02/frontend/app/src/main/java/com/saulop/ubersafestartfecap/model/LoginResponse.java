package com.saulop.ubersafestartfecap.model;

public class LoginResponse {
    private String message;
    private String token;
    private String username;
    private String email;
    private String phone;
    private String type;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getType() {
        return type;
    }
}
