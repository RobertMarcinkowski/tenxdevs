package eu.robm15.tenxdevs.model;

public enum Transport {
    CAR("Car"),
    TRAIN("Train"),
    PLANE("Plane"),
    BUS("Bus"),
    BIKE("Bike"),
    WALKING("Walking");

    private final String displayName;

    Transport(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
