package br.fecap.pi.ubersafestart.model;

public class Achievement {
    private int id;
    private String title;
    private String description;
    private int points;
    private String iconResource;
    private String type;
    private int target;
    private int progress;
    private boolean completed;

    public Achievement(int id, String title, String description, int points,
                       String iconResource, String type, int target, int progress, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.points = points;
        this.iconResource = iconResource;
        this.type = type;
        this.target = target;
        this.progress = progress;
        this.completed = completed;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPoints() { return points; }
    public String getIconResource() { return iconResource; }
    public String getType() { return type; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public boolean isCompleted() { return completed; }

    // Setters se necessário
    public void setProgress(int progress) {
        this.progress = progress;
        this.completed = progress >= target;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Método para calcular a porcentagem de progresso
    public int getProgressPercentage() {
        if (target == 0) return 0;
        return Math.min(100, (progress * 100) / target);
    }
}