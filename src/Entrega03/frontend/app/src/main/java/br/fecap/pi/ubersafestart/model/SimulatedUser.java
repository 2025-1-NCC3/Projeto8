package br.fecap.pi.ubersafestart.model; // Ou o pacote onde você guarda seus modelos

import java.util.Locale;


public class SimulatedUser {
    private String nome;
    private String genero; // "MASCULINO", "FEMININO", "OUTROS"
    private String tipoUsuario; // "MOTORISTA", "PASSAGEIRO"

    // Campos específicos do motorista
    private String modeloCarro;
    private String placaCarro;
    private float notaMotorista; // Rating do motorista (ex: 0.0 a 5.0)

    // Campos específicos do passageiro (exemplo)
    private float safeScorePassageiro; // (ex: 0.0 a 5.0, similar ao ratingBarSafeScore)

    
    public SimulatedUser(String nome, String genero, String modeloCarro, String placaCarro, float notaMotorista) {
        this.nome = nome;
        // Garante que o gênero seja armazenado em maiúsculas e tenha um valor padrão
        this.genero = (genero != null && !genero.isEmpty()) ? genero.toUpperCase(Locale.ROOT) : "OUTROS";
        this.tipoUsuario = "MOTORISTA";
        this.modeloCarro = modeloCarro;
        this.placaCarro = placaCarro;
        this.notaMotorista = notaMotorista;
        this.safeScorePassageiro = 0; // Não aplicável para motorista
    }

    
    public SimulatedUser(String nome, String genero, float safeScorePassageiro) {
        this.nome = nome;
        // Garante que o gênero seja armazenado em maiúsculas e tenha um valor padrão
        this.genero = (genero != null && !genero.isEmpty()) ? genero.toUpperCase(Locale.ROOT) : "OUTROS";
        this.tipoUsuario = "PASSAGEIRO";
        this.safeScorePassageiro = safeScorePassageiro;
        // Campos de motorista ficam nulos ou com valores padrão
        this.modeloCarro = "";
        this.placaCarro = "";
        this.notaMotorista = 0;
    }

    // --- Getters ---

    public String getNome() {
        return nome;
    }

    public String getGenero() {
        return genero;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getModeloCarro() {
        return modeloCarro;
    }

    public String getPlacaCarro() {
        return placaCarro;
    }

    public float getNotaMotorista() {
        return notaMotorista;
    }

    public float getSafeScorePassageiro() {
        return safeScorePassageiro;
    }
}
