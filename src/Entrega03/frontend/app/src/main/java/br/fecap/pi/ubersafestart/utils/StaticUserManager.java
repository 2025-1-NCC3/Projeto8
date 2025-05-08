package br.fecap.pi.ubersafestart.utils; // Ou o pacote onde você guarda seus utilitários

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import br.fecap.pi.ubersafestart.model.SimulatedUser;


public class StaticUserManager {

    private static final List<SimulatedUser> allSimulatedUsers = new ArrayList<>();
    private static final Random random = new Random();

    // Bloco estático para inicializar a lista de usuários simulados
    static {
        // --- MOTORISTAS ---
        allSimulatedUsers.add(new SimulatedUser("Carlos Almeida", "MASCULINO", "VW Nivus", "BRA-2E19", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Fernanda Lima", "FEMININO", "Fiat Pulse", "MER-1C01", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Ricardo Oliveira", "MASCULINO", "Chevrolet Onix", "SUL-3A22", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Beatriz Souza", "FEMININO", "Hyundai HB20", "PAZ-1D34", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Alex Silva", "OUTROS", "Renault Kwid", "AMOR-2B5", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Juliana Pereira", "FEMININO", "Peugeot 208", "LUZ-3C67", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Marcos Costa", "MASCULINO", "Jeep Renegade", "FOR-4E89", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Lia Martins", "OUTROS", "Citroen C3", "VID-5A90", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Gustavo Santos", "MASCULINO", "Honda Civic", "CRF-6G12", 5.0f));
        allSimulatedUsers.add(new SimulatedUser("Camila Ferreira", "FEMININO", "Toyota Yaris", "SPF-C8J3", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Pedro Henrique", "MASCULINO", "Ford Ka", "ABC-0001", 4.5f));
        allSimulatedUsers.add(new SimulatedUser("Sofia Bernardes", "FEMININO", "VW Polo", "DEF-0002", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Lucas Andrade", "MASCULINO", "Fiat Cronos", "GHI-0003", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Manuela Azevedo", "FEMININO", "Renault Sandero", "JKL-0004", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Noah Guimarães", "OUTROS", "Nissan Versa", "MNO-0005", 4.8f));

        // --- PASSAGEIROS ---
        allSimulatedUsers.add(new SimulatedUser("Ana Clara Borges", "FEMININO", 4.5f));
        allSimulatedUsers.add(new SimulatedUser("Bruno Rocha Lima", "MASCULINO", 4.2f));
        allSimulatedUsers.add(new SimulatedUser("Daniela Vieira Dias", "FEMININO", 4.9f));
        allSimulatedUsers.add(new SimulatedUser("Eduardo Matos Silva", "MASCULINO", 3.8f));
        allSimulatedUsers.add(new SimulatedUser("Gabriela Nunes Costa", "FEMININO", 5.0f));
        allSimulatedUsers.add(new SimulatedUser("Kaique Mendes Alves", "OUTROS", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Larissa Dias Pereira", "FEMININO", 4.3f));
        allSimulatedUsers.add(new SimulatedUser("Otávio Barros Gomes", "MASCULINO", 4.6f));
        allSimulatedUsers.add(new SimulatedUser("Samira Borges Ribeiro", "OUTROS", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Tiago Andrade Martins", "MASCULINO", 4.0f));
        allSimulatedUsers.add(new SimulatedUser("Laura Peixoto", "FEMININO", 4.8f));
        allSimulatedUsers.add(new SimulatedUser("Miguel Arantes", "MASCULINO", 4.1f));
        allSimulatedUsers.add(new SimulatedUser("Valentina Correia", "FEMININO", 4.7f));
        allSimulatedUsers.add(new SimulatedUser("Arthur Nogueira", "MASCULINO", 4.4f));
        allSimulatedUsers.add(new SimulatedUser("Helena Siqueira", "OUTROS", 4.9f));
    }

    
    public static SimulatedUser getRandomDriver(String currentUserGender, boolean preferSameGender) {
        // Filtra apenas os motoristas da lista completa
        List<SimulatedUser> potentialDrivers = allSimulatedUsers.stream()
                .filter(user -> "MOTORISTA".equals(user.getTipoUsuario()))
                .collect(Collectors.toList());

        // Retorna null se não houver nenhum motorista na lista estática
        if (potentialDrivers.isEmpty()) {
            return null;
        }

        // Normaliza o gênero do usuário atual para comparação (maiúsculas)
        String normalizedCurrentUserGender = (currentUserGender != null) ? currentUserGender.toUpperCase(Locale.ROOT) : "";

        // Verifica se o filtro de gênero deve ser aplicado
        boolean applyGenderFilter = ("FEMININO".equals(normalizedCurrentUserGender) || "OUTROS".equals(normalizedCurrentUserGender))
                && preferSameGender;

        if (applyGenderFilter) {
            // Filtra os motoristas pelo gênero do usuário atual
            List<SimulatedUser> genderFilteredDrivers = potentialDrivers.stream()
                    .filter(driver -> driver.getGenero().equals(normalizedCurrentUserGender))
                    .collect(Collectors.toList());

            // Se encontrou motoristas do mesmo gênero, retorna um aleatório deles
            if (!genderFilteredDrivers.isEmpty()) {
                return genderFilteredDrivers.get(random.nextInt(genderFilteredDrivers.size()));
            } else {
                // Se a preferência estava ativa mas não encontrou do mesmo gênero, retorna null.
                // Isso força a preferência do usuário.
                return null;
            }
        } else {
            // Se o filtro de gênero não se aplica (usuário é MASCULINO ou preferência está desativada),
            // retorna um motorista aleatório da lista completa de motoristas.
            return potentialDrivers.get(random.nextInt(potentialDrivers.size()));
        }
    }

    
    public static SimulatedUser getRandomPassenger(String currentUserGender, boolean preferSameGender) {
        // Filtra apenas os passageiros da lista completa
        List<SimulatedUser> potentialPassengers = allSimulatedUsers.stream()
                .filter(user -> "PASSAGEIRO".equals(user.getTipoUsuario()))
                .collect(Collectors.toList());

        // Retorna null se não houver nenhum passageiro na lista estática
        if (potentialPassengers.isEmpty()) {
            return null;
        }

        // Normaliza o gênero do usuário atual para comparação (maiúsculas)
        String normalizedCurrentUserGender = (currentUserGender != null) ? currentUserGender.toUpperCase(Locale.ROOT) : "";

        // Verifica se o filtro de gênero deve ser aplicado
        boolean applyGenderFilter = ("FEMININO".equals(normalizedCurrentUserGender) || "OUTROS".equals(normalizedCurrentUserGender))
                && preferSameGender;

        if (applyGenderFilter) {
            // Filtra os passageiros pelo gênero do usuário atual
            List<SimulatedUser> genderFilteredPassengers = potentialPassengers.stream()
                    .filter(passenger -> passenger.getGenero().equals(normalizedCurrentUserGender))
                    .collect(Collectors.toList());

            // Se encontrou passageiros do mesmo gênero, retorna um aleatório deles
            if (!genderFilteredPassengers.isEmpty()) {
                return genderFilteredPassengers.get(random.nextInt(genderFilteredPassengers.size()));
            } else {
                // Se a preferência estava ativa mas não encontrou do mesmo gênero, retorna null.
                return null;
            }
        } else {
            // Se o filtro de gênero não se aplica, retorna um passageiro aleatório da lista completa.
            return potentialPassengers.get(random.nextInt(potentialPassengers.size()));
        }
    }
}
