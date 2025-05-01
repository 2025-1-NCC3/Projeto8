package br.fecap.pi.ubersafestart.model;

public class SafeScoreUpdate {
    private int scoreChange;

    public SafeScoreUpdate(int scoreChange) {
        this.scoreChange = scoreChange;
    }

    public int getScoreChange() {
        return scoreChange;
    }
}