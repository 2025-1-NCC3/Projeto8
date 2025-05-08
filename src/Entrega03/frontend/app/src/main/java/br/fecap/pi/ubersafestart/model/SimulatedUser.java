package br.fecap.pi.ubersafestart.model; // Ajuste o pacote conforme sua estrutura

import java.util.Locale;

public class SimulatedUser {
    private String nome;
    private String gender; // Renomeado para consistência, armazena "MALE", "FEMALE", "OTHER"
    private String userType; // Renomeado para consistência ("DRIVER", "PASSENGER")

    // Campos específicos do motorista
    private String carModel;
    private String licensePlate;
    private float driverRating;

    // Campos específicos do passageiro (exemplo)
    private float passengerSafeScore;

    // Construtor para Motorista
    public SimulatedUser(String nome, String gender, String carModel, String licensePlate, float driverRating) {
        this.nome = nome;
        // Garante MAIÚSCULAS EM INGLÊS
        this.gender = (gender != null && !gender.isEmpty()) ? gender.toUpperCase(Locale.ROOT) : "OTHER";
        this.userType = "DRIVER";
        this.carModel = carModel;
        this.licensePlate = licensePlate;
        this.driverRating = driverRating;
        this.passengerSafeScore = 0;
    }

    // Construtor para Passageiro
    public SimulatedUser(String nome, String gender, float passengerSafeScore) {
        this.nome = nome;
        // Garante MAIÚSCULAS EM INGLÊS
        this.gender = (gender != null && !gender.isEmpty()) ? gender.toUpperCase(Locale.ROOT) : "OTHER";
        this.userType = "PASSENGER";
        this.passengerSafeScore = passengerSafeScore;
        this.carModel = "";
        this.licensePlate = "";
        this.driverRating = 0;
    }

    // Getters (nomes atualizados para inglês para consistência)
    public String getName() { return nome; }
    public String getGender() { return gender; } // Retorna "MALE", "FEMALE", "OTHER"
    public String getUserType() { return userType; } // Retorna "DRIVER", "PASSENGER"
    public String getCarModel() { return carModel; }
    public String getLicensePlate() { return licensePlate; }
    public float getDriverRating() { return driverRating; }
    public float getPassengerSafeScore() { return passengerSafeScore; }
}
