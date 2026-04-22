package com.divine.backendstage1.service;

import com.divine.backendstage1.model.Profile;
import com.divine.backendstage1.repository.ProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProfileSeeder {

    private final ProfileRepository repository;
    private final ObjectMapper mapper;

    public ProfileSeeder(ProfileRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    // Use EventListener instead of PostConstruct

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void seedProfiles() {
        // 1. Quick check to avoid unnecessary file reading if data already exists
        if (repository.count() > 0) {
            return;
        }

        try {
            // 2. Load and Parse JSON
            InputStream inputStream = new ClassPathResource("profiles.json").getInputStream();
            List<Map<String, Object>> profilesData = mapper.readValue(
                    inputStream,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 3. Pre-process incoming names for batch checking
            Set<String> incomingNames = profilesData.stream()
                    .map(data -> ((String) data.getOrDefault("name", "")).toLowerCase().trim())
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.toSet());

            // 4. Fetch all existing names in ONE database hit
            Set<String> existingNamesInDb = repository.findByNameIgnoreCaseIn(incomingNames).stream()
                    .map(p -> p.getName().toLowerCase().trim())
                    .collect(Collectors.toSet());

            List<Profile> newProfilesToSave = new ArrayList<>();

            // 5. Process the data
            for (Map<String, Object> data : profilesData) {
                String name = ((String) data.getOrDefault("name", "")).toLowerCase().trim();

                // Skip if blank or already in the database
                if (name.isBlank() || existingNamesInDb.contains(name)) {
                    continue;
                }

                Profile profile = new Profile();
                profile.setId(Generators.timeBasedEpochGenerator().generate());
                profile.setName(name);
                profile.setGender((String) data.get("gender"));
                profile.setGenderProbability(getDouble(data, "gender_probability"));
                profile.setAge(getInt(data, "age"));
                profile.setAgeGroup(classifyAgeGroup(getInt(data, "age")));
                profile.setCountryId((String) data.get("country_id"));
                profile.setCountryName((String) data.get("country_name"));
                profile.setCountryProbability(getDouble(data, "country_probability"));
                profile.setCreatedAt(Instant.now());

                newProfilesToSave.add(profile);

                // Add to the local set to prevent duplicates if the JSON itself has the same name twice
                existingNamesInDb.add(name);
            }

            // 6. Save everything in ONE batch call
            if (!newProfilesToSave.isEmpty()) {
                repository.saveAll(newProfilesToSave);
                System.out.println("✅ Successfully batch-seeded " + newProfilesToSave.size() + " new profiles.");
            } else {
                System.out.println("ℹ️ No new profiles found to seed.");
            }

        } catch (Exception e) {
            // Using System.err since you used it previously, but log.error is better!
            System.err.println("❌ Failed to seed profiles: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    @EventListener(ContextRefreshedEvent.class)
//    @Transactional
//    public void seedProfiles() {
//        if (repository.count() > 0) {
//            return; // Already seeded
//        }
//
//        try {
//            InputStream inputStream = new ClassPathResource("profiles.json").getInputStream();
//            List<Map<String, Object>> profilesData = mapper.readValue(
//                    inputStream,
//                    new TypeReference<>() {
//                    }
//            );
//
//            for (Map<String, Object> data : profilesData) {
//
//                String name = ((String) data.get("name")).toLowerCase().trim();
//                if (name.isBlank()) continue;
//
//                // Skip if already exists
//                if (repository.findByNameIgnoreCase(name).isPresent()) continue;
//
//
//                Profile profile = new Profile();
//                profile.setId(Generators.timeBasedEpochGenerator().generate());
//                profile.setName(name);
//                profile.setGender((String) data.get("gender"));
//                profile.setGenderProbability(getDouble(data, "gender_probability"));
////                profile.setSampleSize(getInt(data, "sample_size"));
//                profile.setAge(getInt(data, "age"));
//                profile.setAgeGroup(classifyAgeGroup(getInt(data, "age")));
//                profile.setCountryId((String) data.get("country_id"));
//                profile.setCountryName((String) data.get("country_name"));
//                profile.setCountryProbability(getDouble(data, "country_probability"));
//                profile.setCreatedAt(Instant.now());
//
//                repository.save(profile);
//                System.out.println("✅ Seeded " + profilesData.size() + " profiles into the database.");
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to seed profiles: " + e.getMessage());
//        }
//
////        try {
////            // 1. Pre-process names to check: trim, lowercase, and remove blanks
////            Set<String> incomingNames = profilesData.stream()
////                    .map(data -> ((String) data.getOrDefault("name", "")).toLowerCase().trim())
////                    .filter(name -> !name.isBlank())
////                    .collect(Collectors.toSet());
////
////            // 2. Fetch all existing names in ONE query
////            // You'll need to add: List<Profile> findByNameIgnoreCaseIn(Collection<String> names); to your repository
////            Set<String> existingNames = repository.findByNameIgnoreCaseIn(incomingNames).stream()
////                    .map(p -> p.getName().toLowerCase())
////                    .collect(Collectors.toSet());
////
////            List<Profile> newProfiles = new ArrayList<>();
////
////            for (Map<String, Object> data : profilesData) {
////                String name = ((String) data.get("name")).toLowerCase().trim();
////
////                // 3. Skip if blank or already exists (Checking a Set is O(1) - extremely fast)
////                if (name.isBlank() || existingNames.contains(name)) continue;
////
////                Profile profile = new Profile();
////                profile.setId(Generators.timeBasedEpochGenerator().generate());
////                profile.setName(name);
////                // ... set other fields ...
////                profile.setCreatedAt(Instant.now());
////
////                newProfiles.add(profile);
////
////                // Prevent existingNames from having duplicates if profilesData has duplicates
////                existingNames.add(name);
////            }
////
////            // 4. Save all new records in ONE batch call
////            if (!newProfiles.isEmpty()) {
////                repository.saveAll(newProfiles);
////            }
////
////        } catch (Exception e) {
////            log.error("Failed to seed profiles", e); // Better than System.err
////        }
//    }




    @Contract(pure = true)
    private @NotNull String classifyAgeGroup(int age) {
        if (age <= 12) return "child";
        if (age <= 19) return "teenager";
        if (age <= 59) return "adult";
        return "senior";
    }

    private int getInt(@NotNull Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}