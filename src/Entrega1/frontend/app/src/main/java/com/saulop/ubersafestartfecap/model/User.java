package com.saulop.ubersafestartfecap.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String type;
    private String phone;

    public User(String username, String email, String password, String type, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
        this.phone = phone;
    }
}
