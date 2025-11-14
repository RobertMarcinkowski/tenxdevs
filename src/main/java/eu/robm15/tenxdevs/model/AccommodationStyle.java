package eu.robm15.tenxdevs.model;

public enum AccommodationStyle {
    HOTEL("Hotel"),
    HOSTEL("Hostel"),
    APARTMENT("Apartment"),
    CAMPING("Camping"),
    BED_AND_BREAKFAST("Bed & Breakfast"),
    RESORT("Resort");

    private final String displayName;

    AccommodationStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
