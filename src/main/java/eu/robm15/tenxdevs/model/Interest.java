package eu.robm15.tenxdevs.model;

public enum Interest {
    CULTURE("Culture"),
    NATURE("Nature"),
    SPORTS("Sports"),
    GASTRONOMY("Gastronomy"),
    ADVENTURE("Adventure"),
    RELAXATION("Relaxation"),
    HISTORY("History"),
    NIGHTLIFE("Nightlife");

    private final String displayName;

    Interest(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
