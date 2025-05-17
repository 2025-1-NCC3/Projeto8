package br.fecap.pi.ubersafestart.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class AchievementsResponse {
    private boolean success;

    // CORREÇÃO: Adiciona anotações para diferentes formatos possíveis
    @SerializedName(value = "achievementProgress", alternate = {"achievement_progress", "achievements"})
    private List<AchievementProgress> achievementProgress;

    @SerializedName(value = "totalPoints", alternate = {"total_points"})
    private int totalPoints;

    @SerializedName(value = "userLevel", alternate = {"user_level"})
    private int userLevel;

    public static class AchievementProgress {
        @SerializedName(value = "achievementId", alternate = {"achievement_id", "id"})
        private int achievementId;

        private int progress;
        private boolean completed;

        public int getAchievementId() { return achievementId; }
        public int getProgress() { return progress; }
        public boolean isCompleted() { return completed; }
    }

    public boolean isSuccess() { return success; }

    // CORREÇÃO: Nunca retornar null no método getAchievementProgress
    public List<AchievementProgress> getAchievementProgress() {
        if (achievementProgress == null) {
            return new ArrayList<>(); // Retorna lista vazia em vez de null
        }
        return achievementProgress;
    }

    // CORREÇÃO: Valores padrão para nível e pontos totais
    public int getTotalPoints() {
        return totalPoints >= 0 ? totalPoints : 0;
    }

    public int getUserLevel() {
        return userLevel > 0 ? userLevel : 1; // Nível mínimo é 1
    }

    @Override
    public String toString() {
        return "AchievementsResponse{" +
                "success=" + success +
                ", achievementProgress=" + (achievementProgress != null ? achievementProgress.size() : 0) + " items" +
                ", totalPoints=" + totalPoints +
                ", userLevel=" + userLevel +
                '}';
    }
}