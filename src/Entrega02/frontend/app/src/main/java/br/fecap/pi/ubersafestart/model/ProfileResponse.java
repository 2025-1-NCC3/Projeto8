package br.fecap.pi.ubersafestart.model;

public class ProfileResponse {
    private String username;
    private String email;
    private String phone;
    private String type;
    private int safescore;

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

    public int getSafescore() {
        return safescore;
    }
}