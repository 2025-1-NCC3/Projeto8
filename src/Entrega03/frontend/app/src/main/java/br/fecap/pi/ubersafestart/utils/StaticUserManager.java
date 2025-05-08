package br.fecap.pi.ubersafestart.utils; // Ajuste o pacote conforme sua estrutura

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
// Importar a versão CORRIGIDA de SimulatedUser (se alterou no Passo 1)
import br.fecap.pi.ubersafestart.model.SimulatedUser;


public class StaticUserManager {

    private static final List<SimulatedUser> allSimulatedUsers = new ArrayList<>();
    private static final Random random = new Random();

    // Bloco estático para inicializar a lista de usuários simulados
    static {
        // --- MOTORISTAS (Usando formato em Inglês) ---
        allSimulatedUsers.add(new SimulatedUser("Carlos Almeida", "MALE", "VW Nivus", "BRA-2E19", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Fernanda Lima", "FEMALE", "Fiat Pulse", "MER-1C01", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Ricardo Oliveira", "MALE", "Chevrolet Onix", "SUL-3A22", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Beatriz Souza", "FEMALE", "Hyundai HB20", "PAZ-1D34", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Alex Silva", "OTHER", "Renault Kwid", "AMOR-2B5", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Juliana Pereira", "FEMALE", "Peugeot 208", "LUZ-3C67", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Marcos Costa", "MALE", "Jeep Renegade", "FOR-4E89", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Lia Martins", "OTHER", "Citroen C3", "VID-5A90", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Gustavo Santos", "MALE", "Honda Civic", "CRF-6G12", 5.0f));
        allSimulatedUsers.add(new SimulatedUser("Camila Ferreira", "FEMALE", "Toyota Yaris", "SPF-C8J3", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Pedro Henrique", "MALE", "Ford Ka", "ABC-0001", 4.5f));
        allSimulatedUsers.add(new SimulatedUser("Sofia Bernardes", "FEMALE", "VW Polo", "DEF-0002", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Lucas Andrade", "MALE", "Fiat Cronos", "GHI-0003", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Manuela Azevedo", "FEMALE", "Renault Sandero", "JKL-0004", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Noah Guimarães", "OTHER", "Nissan Versa", "MNO-0005", 4.8f));

        // --- PASSAGEIROS (Usando formato em Inglês) ---
        allSimulatedUsers.add(new SimulatedUser("Ana Clara Borges", "FEMALE", 4.5f));
        allSimulatedUsers.add(new SimulatedUser("Bruno Rocha Lima", "MALE", 4.2f));
        allSimulatedUsers.add(new SimulatedUser("Daniela Vieira Dias", "FEMALE", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Eduardo Matos Silva", "MALE", 3.8f));
        allSimulatedUsers.add(new SimulatedUser("Gabriela Nunes Costa", "FEMALE", 5.0f));
        allSimulatedUsers.add(new SimulatedUser("Kaique Mendes Alves", "OTHER", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Larissa Dias Pereira", "FEMALE", 4.3f));
        allSimulatedUsers.add(new SimulatedUser("Otávio Barros Gomes", "MALE", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Samira Borges Ribeiro", "OTHER", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Tiago Andrade Martins", "MALE", 4.0f));
        allSimulatedUsers.add(new SimulatedUser("Laura Peixoto", "FEMALE", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Miguel Arantes", "MALE", 4.1f));
        allSimulatedUsers.add(new SimulatedUser("Valentina Correia", "FEMALE", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Arthur Nogueira", "MALE", 4.4f));
        allSimulatedUsers.add(new SimulatedUser("Helena Siqueira", "OTHER", 4.9f));
    }


    public static SimulatedUser getRandomDriver(String currentUserGender, boolean preferSameGender) {
        // Filtra apenas os motoristas
        List<SimulatedUser> potentialDrivers = allSimulatedUsers.stream()
                // Use getUserType() se alterou SimulatedUser, senão use getTipoUsuario()
                .filter(user -> "DRIVER".equals(user.getUserType())) // ou "MOTORISTA"
                .collect(Collectors.toList());

        if (potentialDrivers.isEmpty()) {
            return null;
        }

        // O currentUserGender já vem normalizado (MAIÚSCULAS EM INGLÊS) da Activity
        String normalizedCurrentUserGender = currentUserGender; // Não precisa normalizar de novo

        // *** CORREÇÃO AQUI: Comparar com MAIÚSCULAS EM INGLÊS ***
        boolean applyGenderFilter = ("FEMALE".equals(normalizedCurrentUserGender) || "OTHER".equals(normalizedCurrentUserGender))
                && preferSameGender;

        if (applyGenderFilter) {
            List<SimulatedUser> genderFilteredDrivers = potentialDrivers.stream()
                    // *** CORREÇÃO AQUI: Comparar géneros normalizados ***
                    // Use getGender() se alterou SimulatedUser, senão use getGenero()
                    .filter(driver -> driver.getGender().equals(normalizedCurrentUserGender))
                    .collect(Collectors.toList());

            if (!genderFilteredDrivers.isEmpty()) {
                return genderFilteredDrivers.get(random.nextInt(genderFilteredDrivers.size()));
            } else {
                return null; // Não encontrou do mesmo género com preferência ativa
            }
        } else {
            // Retorna um motorista aleatório da lista completa
            return potentialDrivers.get(random.nextInt(potentialDrivers.size()));
        }
    }


    public static SimulatedUser getRandomPassenger(String currentUserGender, boolean preferSameGender) {
        // Filtra apenas os passageiros
        List<SimulatedUser> potentialPassengers = allSimulatedUsers.stream()
                // Use getUserType() se alterou SimulatedUser, senão use getTipoUsuario()
                .filter(user -> "PASSENGER".equals(user.getUserType())) // ou "PASSAGEIRO"
                .collect(Collectors.toList());

        if (potentialPassengers.isEmpty()) {
            return null;
        }

        // O currentUserGender já vem normalizado (MAIÚSCULAS EM INGLÊS) da Activity
        String normalizedCurrentUserGender = currentUserGender;

        // *** CORREÇÃO AQUI: Comparar com MAIÚSCULAS EM INGLÊS ***
        boolean applyGenderFilter = ("FEMALE".equals(normalizedCurrentUserGender) || "OTHER".equals(normalizedCurrentUserGender))
                && preferSameGender;

        if (applyGenderFilter) {
            List<SimulatedUser> genderFilteredPassengers = potentialPassengers.stream()
                    // *** CORREÇÃO AQUI: Comparar géneros normalizados ***
                    // Use getGender() se alterou SimulatedUser, senão use getGenero()
                    .filter(passenger -> passenger.getGender().equals(normalizedCurrentUserGender))
                    .collect(Collectors.toList());

            if (!genderFilteredPassengers.isEmpty()) {
                return genderFilteredPassengers.get(random.nextInt(genderFilteredPassengers.size()));
            } else {
                return null; // Não encontrou do mesmo género com preferência ativa
            }
        } else {
            // Retorna um passageiro aleatório da lista completa
            return potentialPassengers.get(random.nextInt(potentialPassengers.size()));
        }
    }
}
