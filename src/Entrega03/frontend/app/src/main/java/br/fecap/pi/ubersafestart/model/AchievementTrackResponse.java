package br.fecap.pi.ubersafestart.model;

import java.util.List;

public class AchievementTrackResponse {
    private boolean success;
    private List<UpdatedAchievement> updatedAchievements;
    private List<CompletedAchievement> newlyCompleted;

    public static class UpdatedAchievement {
        private int achievementId;
        private int progress;
        private boolean completed;

        public int getAchievementId() { return achievementId; }
        public int getProgress() { return progress; }
        public boolean isCompleted() { return completed; }
    }

    public static class CompletedAchievement {
        private int achievementId;
        private int points;

        public int getAchievementId() { return achievementId; }
        public int getPoints() { return points; }
    }

    public boolean isSuccess() { return success; }
    public List<UpdatedAchievement> getUpdatedAchievements() { return updatedAchievements; }
    public List<CompletedAchievement> getNewlyCompleted() { return newlyCompleted; }
}