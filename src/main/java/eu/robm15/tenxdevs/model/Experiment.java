package eu.robm15.tenxdevs.model;

import jakarta.persistence.*;

@Entity
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Experiment setName(String name) {
        this.name = name;
        return this;
    }

}
