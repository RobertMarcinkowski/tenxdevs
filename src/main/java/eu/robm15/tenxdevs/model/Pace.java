package eu.robm15.tenxdevs.model;

public enum Pace {
    RELAXED("Relaxed"),
    MODERATE("Moderate"),
    FAST_PACED("Fast-paced");

    private final String displayName;

    Pace(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
