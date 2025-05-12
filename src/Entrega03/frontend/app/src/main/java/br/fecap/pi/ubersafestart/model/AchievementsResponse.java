package br.fecap.pi.ubersafestart.model;

import java.util.List;

public class AchievementsResponse {
    private boolean success;
    private List<AchievementProgress> achievementProgress;
    private int totalPoints;
    private int userLevel;

    public static class AchievementProgress {
        private int achievementId;
        private int progress;
        private boolean completed;

        public int getAchievementId() { return achievementId; }
        public int getProgress() { return progress; }
        public boolean isCompleted() { return completed; }
    }

    public boolean isSuccess() { return success; }
    public List<AchievementProgress> getAchievementProgress() { return achievementProgress; }
    public int getTotalPoints() { return totalPoints; }
    public int getUserLevel() { return userLevel; }
}