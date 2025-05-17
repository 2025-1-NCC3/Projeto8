package br.fecap.pi.ubersafestart.model;

import com.google.gson.annotations.SerializedName;

public class SafeScoreResponse {
    private boolean success;
    private String message;

    // Anotação para garantir que reconheça os dois formatos possíveis do servidor
    @SerializedName(value = "newScore", alternate = {"new_score"})
    private int newScore;

    // ***** INÍCIO DA MODIFICAÇÃO *****
    // Adicionado "score" aos nomes alternativos para mapear corretamente a resposta da API
    @SerializedName(value = "safescore", alternate = {"safeScore", "safe_score", "score"})
    // ***** FIM DA MODIFICAÇÃO *****
    private int safescore;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getNewScore() {
        return newScore;
    }

    public int getSafescore() {
        // Este getter agora retornará o valor do campo "score" se ele estiver presente no JSON
        return safescore;
    }

    // NOVO: Método para obter o melhor valor disponível
    // Este método continuará funcionando corretamente.
    // Se 'score' (agora mapeado para 'safescore') for 100 e 'newScore' for 0 (ou ausente),
    // ele retornará 100.
    public int getBestAvailableScore() {
        if (newScore > 0) return newScore;
        if (safescore > 0) return safescore;
        return 0; // Ambos são zero ou negativos, ou não encontrados
    }

    @Override
    public String toString() {
        return "SafeScoreResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", newScore=" + newScore +
                ", safescore=" + safescore + // (que agora pode vir do campo "score" do JSON)
                '}';
    }
}