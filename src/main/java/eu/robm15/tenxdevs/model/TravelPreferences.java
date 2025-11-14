package eu.robm15.tenxdevs.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing user travel preferences for VibeTravels application.
 * Stores seven categories of preferences to personalize trip planning.
 */
@Entity
@Table(name = "travel_preferences")
public class TravelPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /**
     * Supabase user ID - links preferences to authenticated user
     */
    @Column(nullable = false, unique = true)
    private String userId;

    /**
     * Budget preference
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Budget budget;

    /**
     * Travel pace preference
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Pace pace;

    /**
     * Interests/hobbies - allows multiple selections
     */
    @ElementCollection(targetClass = Interest.class)
    @CollectionTable(name = "travel_preferences_interests", joinColumns = @JoinColumn(name = "travel_preferences_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest")
    private Set<Interest> interests = new HashSet<>();

    /**
     * Accommodation style preference
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AccommodationStyle accommodationStyle;

    /**
     * Preferred transport methods - allows multiple selections
     */
    @ElementCollection(targetClass = Transport.class)
    @CollectionTable(name = "travel_preferences_transport", joinColumns = @JoinColumn(name = "travel_preferences_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "transport")
    private Set<Transport> transport = new HashSet<>();

    /**
     * Food preferences - allows multiple selections
     */
    @ElementCollection(targetClass = FoodPreference.class)
    @CollectionTable(name = "travel_preferences_food", joinColumns = @JoinColumn(name = "travel_preferences_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "food_preference")
    private Set<FoodPreference> foodPreferences = new HashSet<>();

    /**
     * Preferred travel season
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Season season;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public Pace getPace() {
        return pace;
    }

    public void setPace(Pace pace) {
        this.pace = pace;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public AccommodationStyle getAccommodationStyle() {
        return accommodationStyle;
    }

    public void setAccommodationStyle(AccommodationStyle accommodationStyle) {
        this.accommodationStyle = accommodationStyle;
    }

    public Set<Transport> getTransport() {
        return transport;
    }

    public void setTransport(Set<Transport> transport) {
        this.transport = transport;
    }

    public Set<FoodPreference> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(Set<FoodPreference> foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }
}
