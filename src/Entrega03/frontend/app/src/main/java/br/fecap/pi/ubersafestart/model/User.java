package br.fecap.pi.ubersafestart.model;

// Importar a anotação do GSON
import com.google.gson.annotations.SerializedName;

/**
 * Modelo de dados para o registro de usuário, enviado para a API /api/auth/signup.
 */
public class User {
    // Campos existentes
    private String username;
    private String email;
    private String password;
    private String type;
    private String phone;

    // --- Campo de Gênero Corrigido ---
    // Anotação para garantir que no JSON o nome seja "gender"
    @SerializedName("gender")
    private String gender; // Nome da variável Java alterado para 'gender' para clareza

    /**
     * Construtor para criar um objeto User para registro.
     *
     * @param username Nome de usuário.
     * @param email Email do usuário.
     * @param password Senha (será hasheada no backend).
     * @param type Tipo de usuário ('passenger' ou 'driver').
     * @param phone Telefone do usuário (opcional).
     * @param gender Gênero do usuário ('male', 'female', 'other').
     */
    public User(String username, String email, String password, String type, String phone, String gender) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
        this.phone = phone;
        this.gender = gender; // Atribui ao campo 'gender'
    }

    // --- Getters (Adapte ou adicione se necessário) ---

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Geralmente não é necessário um getter para a senha no modelo de envio
    // public String getPassword() { return password; }

    public String getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    // Getter para o campo de gênero corrigido
    public String getGender() {
        return gender;
    }

    // --- Setters (Opcionais, dependendo de como você constrói o objeto) ---
    // Exemplo:
    // public void setGender(String gender) {
    //     this.gender = gender;
    // }
}
