package com.divine.backendstage1.service;

import com.divine.backendstage1.model.Profile;
import com.divine.backendstage1.repository.ProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ExternalApiService externalApiService;
    ObjectMapper mapper = new ObjectMapper();

    public ProfileService(ProfileRepository profileRepository,
                          ExternalApiService externalApiService) {
        this.profileRepository = profileRepository;
        this.externalApiService = externalApiService;
    }

    // ---------- CREATE ----------
    public Map<String, Object> createProfile(String name) {

        // 1. Check if profile already exists (idempotency)
        Optional<Profile> existing = profileRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Profile already exists");
            response.put("data", formatProfile(existing.get()));
            return response;
        }

        // 2. Call all 3 APIs in parallel
        CompletableFuture<Map<String, Object>> genderizeFuture =
                externalApiService.callGenderizeAsync(name);
        CompletableFuture<Map<String, Object>> agifyFuture =
                externalApiService.callAgifyAsync(name);
        CompletableFuture<Map<String, Object>> nationalizeFuture =
                externalApiService.callNationalizeAsync(name);

        // Wait for all 3 to complete
        CompletableFuture.allOf(genderizeFuture, agifyFuture, nationalizeFuture).join();

        Map<String, Object> genderizeData;
        Map<String, Object> agifyData;
        Map<String, Object> nationalizeData;

        try {
            genderizeData = genderizeFuture.get();
            agifyData = agifyFuture.get();
            nationalizeData = nationalizeFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("UPSTREAM_ERROR: upstream api call failed");
        }

        // 3. Validate Genderize response
        String gender = (String) genderizeData.get("gender");
        Object countObj = genderizeData.get("count");
        int count = countObj != null ? ((Number) countObj).intValue() : 0;
        if (gender == null || count == 0) {
            throw new RuntimeException("UPSTREAM_ERROR: Genderize returned an invalid response");
        }

        // 4. Validate Agify response
        Object ageObj = agifyData.get("age");
        if (ageObj == null) {
            throw new RuntimeException("UPSTREAM_ERROR: Agify returned an invalid response");
        }
        int age = ((Number) ageObj).intValue();

        // 5. Validate Nationalize response
        List<Map<String, Object>> countries =
                mapper.convertValue(
                        nationalizeData.get("country"),
                        new TypeReference<List<Map<String, Object>>>() {});
        if (countries == null || countries.isEmpty()) {
            throw new RuntimeException("UPSTREAM_ERROR: Nationalize returned an invalid response");
        }

        // 6. Pick country with highest probability
        Map<String, Object> topCountry = countries.stream()
            .max(Comparator.comparingDouble(
                        c -> ((Number) c.get("probability")).doubleValue()))
                .orElseThrow(() ->
                        new RuntimeException("UPSTREAM_ERROR: Nationalize returned an invalid response"));

        String countryId = (String) topCountry.get("country_id");
        double countryProbability = ((Number) topCountry.get("probability")).doubleValue();

        // 7. Classify age group
        String ageGroup = classifyAgeGroup(age);

        // 8. Build and save profile
        Profile profile = new Profile();
        profile.setId(Generators.timeBasedEpochGenerator().generate()); // UUID v7
        profile.setName(name.toLowerCase());
        profile.setGender(gender);
        profile.setGenderProbability(((Number) genderizeData.get("probability")).doubleValue());
        profile.setSampleSize(count);
        profile.setAge(age);
        profile.setAgeGroup(ageGroup);
        profile.setCountryId(countryId);
        profile.setCountryProbability(countryProbability);
        profile.setCreatedAt(Instant.now());

        profileRepository.save(profile);

        // 9. Return 201 response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("data", formatProfile(profile));
        return response;
    }

    // ---------- GET SINGLE ----------
    public Map<String, Object> getProfile(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND: Profile not found"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("data", formatProfile(profile));
        return response;
    }

    // ---------- GET ALL WITH FILTERS ----------
    public Map<String, Object> getAllProfiles(String gender, String countryId, String ageGroup) {
        List<Profile> profiles;

        boolean hasGender = gender != null && !gender.isEmpty();
        boolean hasCountry = countryId != null && !countryId.isEmpty();
        boolean hasAgeGroup = ageGroup != null && !ageGroup.isEmpty();

        if (hasGender && hasCountry && hasAgeGroup) {
            profiles = profileRepository
                    .findByGenderIgnoreCaseAndCountryIdIgnoreCaseAndAgeGroupIgnoreCase(
                            gender, countryId, ageGroup);
        } else if (hasGender && hasCountry) {
            profiles = profileRepository
                    .findByGenderIgnoreCaseAndCountryIdIgnoreCase(gender, countryId);
        } else if (hasGender && hasAgeGroup) {
            profiles = profileRepository
                    .findByGenderIgnoreCaseAndAgeGroupIgnoreCase(gender, ageGroup);
        } else if (hasCountry && hasAgeGroup) {
            profiles = profileRepository
                    .findByCountryIdIgnoreCaseAndAgeGroupIgnoreCase(countryId, ageGroup);
        } else if (hasGender) {
            profiles = profileRepository.findByGenderIgnoreCase(gender);
        } else if (hasCountry) {
            profiles = profileRepository.findByCountryIdIgnoreCase(countryId);
        } else if (hasAgeGroup) {
            profiles = profileRepository.findByAgeGroupIgnoreCase(ageGroup);
        } else {
            profiles = profileRepository.findAll();
        }

        List<Map<String, Object>> dataList = profiles.stream()
                .map(this::formatProfileList)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("count", dataList.size());
        response.put("data", dataList);
        return response;
    }

    // ---------- DELETE ----------
    public void deleteProfile(UUID id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("NOT_FOUND: Profile not found / Profile already deleted");
        }
        profileRepository.deleteById(id);
    }

    // ---------- HELPERS ----------

    @Contract(pure = true)
    private @NotNull String classifyAgeGroup(int age) {
        if (age <= 12) return "child";
        if (age <= 19) return "teenager";
        if (age <= 59) return "adult";
        return "senior";
    }

    // Full profile format (for POST and GET single)
    private @NotNull Map<String, Object> formatProfile(@NotNull Profile p) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", p.getId().toString());
        data.put("name", p.getName());
        data.put("gender", p.getGender());
        data.put("gender_probability", p.getGenderProbability());
        data.put("sample_size", p.getSampleSize());
        data.put("age", p.getAge());
        data.put("age_group", p.getAgeGroup());
        data.put("country_id", p.getCountryId());
        data.put("country_probability", p.getCountryProbability());
        data.put("created_at", p.getCreatedAt().toString());
        return data;
    }

    // List profile format (for GET all)
    private @NotNull Map<String, Object> formatProfileList(@NotNull Profile p) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", p.getId().toString());
        data.put("name", p.getName());
        data.put("gender", p.getGender());
        data.put("age", p.getAge());
        data.put("age_group", p.getAgeGroup());
        data.put("country_id", p.getCountryId());
        return data;
    }
}