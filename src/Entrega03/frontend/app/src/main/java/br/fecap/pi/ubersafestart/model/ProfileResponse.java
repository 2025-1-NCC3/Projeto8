package br.fecap.pi.ubersafestart.model;

// Não precisa de import do GSON se o nome do campo Java for igual à chave JSON

/**
 * Modelo de dados para a resposta da API /api/profile.
 * Contém as informações do perfil do usuário.
 */
public class ProfileResponse {
    private String username;
    private String email;
    private String phone;
    private String type;
    private int safescore;

    // --- CAMPO DE GÊNERO CORRIGIDO ---
    // O nome do campo Java ('gender') agora corresponde exatamente
    // à chave JSON ('gender') enviada pelo backend.
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

    public int getSafescore() {
        return safescore;
    }

    /**
     * Retorna o gênero do usuário como recebido do backend (ex: 'male', 'female', 'other').
     * O ProfileActivity deve usar este getter.
     * @return O gênero do usuário ou null se não informado.
     */
    public String getGender() {
        return gender;
    }

    // O campo 'genero' e o getter getGenero() foram removidos para evitar ambiguidade.
}
