package br.fecap.pi.ubersafestart.model;

public class SafetyTip {
    private String title;
    private String shortDescription;
    private String longDescription;
    private int iconResId;
    private boolean expanded;

    public SafetyTip(String title, String shortDescription, String longDescription, int iconResId) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.iconResId = iconResId;
        this.expanded = false;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}