package eu.robm15.tenxdevs.model;

public enum Budget {
    BUDGET("Budget"),
    MODERATE("Moderate"),
    LUXURY("Luxury");

    private final String displayName;

    Budget(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
