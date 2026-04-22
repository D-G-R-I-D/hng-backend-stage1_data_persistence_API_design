package com.divine.backendstage1.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name = "gender_probability")
    private Double genderProbability;

    @Column(name = "age")
    private Integer age;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "country_id")
    private String countryId;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "country_probability")
    private Double countryProbability;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean isNew = true;  // treats entity as new by default

//    @Override
//    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }

    @PostLoad  // marks it as NOT new when loaded from DB
    void markNotNew() { this.isNew = false; }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getGenderProbability() { return genderProbability; }
    public void setGenderProbability(Double genderProbability) { this.genderProbability = genderProbability; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getCountryId() { return countryId; }
    public void setCountryId(String countryId) { this.countryId = countryId; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public Double getCountryProbability() { return countryProbability; }
    public void setCountryProbability(Double countryProbability) { this.countryProbability = countryProbability; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}