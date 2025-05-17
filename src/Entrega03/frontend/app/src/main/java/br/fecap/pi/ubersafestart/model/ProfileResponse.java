package br.fecap.pi.ubersafestart.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de dados para a resposta da API /api/profile.
 * Contém as informações do perfil do usuário.
 */
public class ProfileResponse {
    private String username;
    private String email;
    private String phone;
    private String type;

    @SerializedName(value = "safescore", alternate = {"safeScore", "safe_score"})
    private int safescore;

    private String gender;

    // --- Getters ---

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

    // CORREÇÃO: Se o valor for zero, retorna um valor padrão (50)
    public int getSafescore() {
        // Retorna o valor do servidor, ou 50 se o valor for zero ou negativo
        return (safescore > 0) ? safescore : 50;
    }

    /**
     * Retorna o gênero do usuário como recebido do backend (ex: 'male', 'female', 'other').
     * O ProfileActivity deve usar este getter.
     * @return O gênero do usuário ou null se não informado.
     */
    public String getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "ProfileResponse{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", type='" + type + '\'' +
                ", safescore=" + safescore +
                ", gender='" + gender + '\'' +
                '}';
    }
}