package com.divine.backendstage1.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "gender")
    private String gender;

    @Column(name = "gender_probability")
    private Double genderProbability;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "age")
    private Integer age;

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "country_id")
    private String countryId;

    @Column(name = "country_probability")
    private Double countryProbability;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getGenderProbability() { return genderProbability; }
    public void setGenderProbability(Double genderProbability) { this.genderProbability = genderProbability; }

    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getCountryId() { return countryId; }
    public void setCountryId(String countryId) { this.countryId = countryId; }

    public Double getCountryProbability() { return countryProbability; }
    public void setCountryProbability(Double countryProbability) { this.countryProbability = countryProbability; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
