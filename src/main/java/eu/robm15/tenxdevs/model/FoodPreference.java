package eu.robm15.tenxdevs.model;

public enum FoodPreference {
    LOCAL_CUISINE("Local Cuisine"),
    INTERNATIONAL("International"),
    FAST_FOOD("Fast Food"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    STREET_FOOD("Street Food"),
    FINE_DINING("Fine Dining");

    private final String displayName;

    FoodPreference(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
