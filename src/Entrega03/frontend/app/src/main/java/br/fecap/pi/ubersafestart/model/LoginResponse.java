// Arquivo: br/fecap/pi/ubersafestart/model/LoginResponse.java
package br.fecap.pi.ubersafestart.model;

public class LoginResponse {
    private String message;
    private String token;
    private String username;
    private String email;
    private String phone;
    private String type;
    private String userId;
    private int safeScore;
    private String genero; // Gênero CONTINUA AQUI

    // Getters
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getType() { return type; }
    public String getUserId() { return userId; }
    public int getSafeScore() { return safeScore; }
    public String getGenero() { return genero; } // Getter para gênero
}