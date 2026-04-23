package com.divine.backendstage1.service;

import com.divine.backendstage1.model.Profile;
import com.divine.backendstage1.repository.ProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.divine.backendstage1.service.NaturalLanguageParser;
import com.divine.backendstage1.repository.ProfileSpecification;
import static com.divine.backendstage1.repository.ProfileSpecification.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ExternalApiService externalApiService;
    private final ObjectMapper mapper;
//    private final ProfileSeeder profileSeeder;
    private final NaturalLanguageParser naturalLanguageParser;

    private static final Map<String, String> COUNTRY_NAMES = Map.<String, String>ofEntries(
            // Africa
            Map.entry("NG", "Nigeria"),
            Map.entry("GH", "Ghana"),
            Map.entry("KE", "Kenya"),
            Map.entry("ZA", "South Africa"),
            Map.entry("ET", "Ethiopia"),
            Map.entry("AO", "Angola"),
            Map.entry("BJ", "Benin"),
            Map.entry("TG", "Togo"),
            Map.entry("CI", "Ivory Coast"),
            Map.entry("SN", "Senegal"),
            Map.entry("CM", "Cameroon"),
            Map.entry("UG", "Uganda"),
            Map.entry("RW", "Rwanda"),
            Map.entry("TZ", "Tanzania"),
            Map.entry("MZ", "Mozambique"),
            Map.entry("ZM", "Zambia"),
            Map.entry("ZW", "Zimbabwe"),
            Map.entry("MW", "Malawi"),
            Map.entry("BW", "Botswana"),
            Map.entry("NA", "Namibia"),
            Map.entry("ML", "Mali"),
            Map.entry("BF", "Burkina Faso"),
            Map.entry("NE", "Niger"),
            Map.entry("TD", "Chad"),
            Map.entry("SD", "Sudan"),
            Map.entry("SS", "South Sudan"),
            Map.entry("ER", "Eritrea"),
            Map.entry("SO", "Somalia"),
            Map.entry("DJ", "Djibouti"),
            Map.entry("MR", "Mauritania"),
            Map.entry("GM", "Gambia"),
            Map.entry("GN", "Guinea"),
            Map.entry("GW", "Guinea-Bissau"),
            Map.entry("SL", "Sierra Leone"),
            Map.entry("LR", "Liberia"),
            Map.entry("MA", "Morocco"),
            Map.entry("DZ", "Algeria"),
            Map.entry("TN", "Tunisia"),
            Map.entry("LY", "Libya"),
            Map.entry("EG", "Egypt"),
            // Europe
            Map.entry("GB", "United Kingdom"),
            Map.entry("DE", "Germany"),
            Map.entry("FR", "France"),
            Map.entry("IT", "Italy"),
            Map.entry("ES", "Spain"),
            Map.entry("PT", "Portugal"),
            Map.entry("NL", "Netherlands"),
            Map.entry("BE", "Belgium"),
            Map.entry("CH", "Switzerland"),
            Map.entry("SE", "Sweden"),
            Map.entry("NO", "Norway"),
            Map.entry("DK", "Denmark"),
            Map.entry("FI", "Finland"),
            Map.entry("PL", "Poland"),
            Map.entry("RU", "Russia"),
            Map.entry("UA", "Ukraine"),
            Map.entry("CZ", "Czech Republic"),
            Map.entry("RO", "Romania"),
            Map.entry("HU", "Hungary"),
            Map.entry("GR", "Greece"),
            Map.entry("AT", "Austria"),
            Map.entry("IE", "Ireland"),
            // Americas
            Map.entry("US", "United States"),
            Map.entry("CA", "Canada"),
            Map.entry("BR", "Brazil"),
            Map.entry("MX", "Mexico"),
            Map.entry("AR", "Argentina"),
            Map.entry("CO", "Colombia"),
            Map.entry("CL", "Chile"),
            Map.entry("PE", "Peru"),
            Map.entry("VE", "Venezuela"),
            Map.entry("EC", "Ecuador"),
            Map.entry("BO", "Bolivia"),
            Map.entry("PY", "Paraguay"),
            Map.entry("UY", "Uruguay"),
            // Asia
            Map.entry("CN", "China"),
            Map.entry("JP", "Japan"),
            Map.entry("IN", "India"),
            Map.entry("PK", "Pakistan"),
            Map.entry("BD", "Bangladesh"),
            Map.entry("ID", "Indonesia"),
            Map.entry("PH", "Philippines"),
            Map.entry("VN", "Vietnam"),
            Map.entry("TH", "Thailand"),
            Map.entry("KR", "South Korea"),
            Map.entry("TR", "Turkey"),
            Map.entry("SA", "Saudi Arabia"),
            Map.entry("AE", "United Arab Emirates"),
            Map.entry("IQ", "Iraq"),
            Map.entry("IR", "Iran"),
            Map.entry("SY", "Syria"),
            Map.entry("MY", "Malaysia"),
            Map.entry("MM", "Myanmar"),
            // Oceania
            Map.entry("AU", "Australia"),
            Map.entry("NZ", "New Zealand")
    );


    public ProfileService(ProfileRepository profileRepository,
                          ExternalApiService externalApiService,
                          ObjectMapper mapper,
//                          ProfileSeeder profileSeeder,
                          NaturalLanguageParser naturalLanguageParser1) {  // Inject shared ObjectMapper
        this.profileRepository = profileRepository;
        this.externalApiService = externalApiService;
        this.mapper = mapper;
//        this.profileSeeder = profileSeeder;
        this.naturalLanguageParser = naturalLanguageParser1;
    }

    // ---------- CREATE ----------
    public Map<String, Object> createProfile(String name) {

        // 1. Idempotency check
        Optional<Profile> existing = profileRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            return Map.of(
                    "status", "exists",   // use a different key so controller can detect it
                    "data", formatProfile(existing.get())
            );
        }

        // 2. Call all 3 APIs in parallel
        CompletableFuture<Map<String, Object>> genderizeFuture =
                externalApiService.callGenderizeAsync(name);
        CompletableFuture<Map<String, Object>> agifyFuture =
                externalApiService.callAgifyAsync(name);
        CompletableFuture<Map<String, Object>> nationalizeFuture =
                externalApiService.callNationalizeAsync(name);

        // 3. Wait for all to complete
        CompletableFuture.allOf(genderizeFuture, agifyFuture, nationalizeFuture).join();

        // 4. Extract results (join() is safe here - futures are already done)
        Map<String, Object> genderizeData = genderizeFuture.join();
        Map<String, Object> agifyData = agifyFuture.join();
        Map<String, Object> nationalizeData = nationalizeFuture.join();

        // 5. Parse responses with graceful defaults
        String gender = Optional.ofNullable(genderizeData.get("gender"))
                .map(Object::toString)
                .orElse("unknown");

        int count = Optional.ofNullable(genderizeData.get("count"))
                .map(n -> ((Number) n).intValue())
                .orElse(0);

        double genderProbability = Optional.ofNullable(genderizeData.get("probability"))
                .map(n -> ((Number) n).doubleValue())
                .orElse(0.0);

        int age = Optional.ofNullable(agifyData.get("age"))
                .map(n -> ((Number) n).intValue())
                .orElse(0);

        // 6. Parse country data
        String countryId = "unknown";
        String countryName = "Unknown";
        double countryProbability = 0.0;

        try {
            List<Map<String, Object>> countries = mapper.convertValue(
                    nationalizeData.get("country"),
                    new TypeReference<>() {});

            if (countries != null && !countries.isEmpty()) {
                Map<String, Object> topCountry = countries.stream()
                        .max(Comparator.comparingDouble(
                                c -> ((Number) c.get("probability")).doubleValue()))
                        .orElse(null);

                countryId = Optional.ofNullable(topCountry.get("country_id"))
                        .map(Object::toString)
                        .orElse("unknown");

                countryName = COUNTRY_NAMES.getOrDefault(countryId, countryId);

                countryProbability = Optional.ofNullable(topCountry.get("probability"))
                        .map(n -> ((Number) n).doubleValue())
                        .orElse(0.0);
            }
        } catch (Exception e) {
            // Country parsing failed - defaults remain "unknown" / 0.0
        }

        // 7. Build and save profile
        Profile profile = new Profile();
        profile.setId(Generators.timeBasedEpochGenerator().generate());
        profile.setName(name.toLowerCase());
        profile.setGender(gender);
        profile.setGenderProbability(genderProbability);
//        profile.setSampleSize(count);
        profile.setAge(age);
        profile.setAgeGroup(classifyAgeGroup(age));
        profile.setCountryId(countryId);
        profile.setCountryName(countryName);
        profile.setCountryProbability(countryProbability);
        profile.setCreatedAt(Instant.now());

        profileRepository.save(profile);

        return Map.of(
                "status", "success",
                "data", formatProfile(profile)
        );
    }

    // ---------- GET SINGLE ----------
    public Map<String, Object> getProfile(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND: Profile not found"));

        return Map.of(
                "status", "success",
                "data", formatProfile(profile)
        );
    }

    // ---------- GET ALL ----------
//    public Map<String, Object> getAllProfiles(String gender, String countryId, String ageGroup) {
//        List<Profile> profiles = findByFilters(gender, countryId, ageGroup);
//
//        return Map.of(
//                "status", "success",
//                "count", profiles.size(),
//                "data", profiles.stream()
//                        .map(this::formatProfileList)
//                        .toList()
//        );
//    }

    // ==================== ADVANCED GET ALL ====================
//    public Map<String, Object> getAllProfiles(
//            String gender, String ageGroup, String countryId,
//            Integer minAge, Integer maxAge,
//            Double minGenderProb, Double minCountryProb,
//            String sortBy, String order,
//            int page, int limit) {
//
//
//        Sort.Direction direction = "desc".equalsIgnoreCase(order)
//                ? Sort.Direction.DESC : Sort.Direction.ASC;
//
//        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "created_at") {
//            case "age" -> Sort.by(direction, "age");
//            case "gender_probability" -> Sort.by(direction, "genderProbability");
//            case "created_at" -> Sort.by(direction, "createdAt");
//            default -> Sort.by(direction, "createdAt");
//        };
//
//        Pageable pageable = PageRequest.of(page - 1, limit, sort);
//
//        Specification<Profile> spec = Specification
//                .where(hasGender(gender))
//                .and(hasAgeGroup(ageGroup))
//                .and(hasCountryId(countryId))
//                .and(minAge(minAge))
//                .and(maxAge(maxAge))
//                .and(minGenderProbability(minGenderProb))
//                .and(minCountryProbability(minCountryProb));
//
//        int effectiveLimit = Math.min(limit, 50);
//        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);
//
//        List<Map<String, Object>> data = profilePage.getContent().stream()
//                .map(this::formatProfileList)
//                .toList();
//
//        return new LinkedHashMap<>() {{
//            put("status", "success");
//            put("page", page);
//            put("limit", effectiveLimit);
//            put("total", profilePage.getTotalElements());
//            put("total_pages", profilePage.getTotalPages());
//            put("has_next", profilePage.hasNext());
//            put("has_previous", profilePage.hasPrevious());
//            put("data", data);
//        }};
//    }
//

    public Map<String, Object> getAllProfiles(
            String gender, String ageGroup, String countryId,
            Integer minAge, Integer maxAge,
            Double minGenderProb, Double minCountryProb,
            String sortBy, String order,
            int page, int limit) {

        // Clamp values
        if (page < 1) page = 1;
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50;

        // Direction
        Sort.Direction direction = (order != null && order.equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Sort field — map external name to entity field name
        String sortField;
        if (sortBy == null || sortBy.isBlank()) {
            sortField = "createdAt";
        } else {
            sortField = switch (sortBy.toLowerCase().trim()) {
                case "age"                -> "age";
                case "gender_probability" -> "genderProbability";
                case "created_at"         -> "createdAt";
                default                   -> "createdAt";
            };
        }

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));

        Specification<Profile> spec = Specification
                .where(ProfileSpecification.hasGender(gender))
                .and(ProfileSpecification.hasAgeGroup(ageGroup))
                .and(ProfileSpecification.hasCountryId(countryId))
                .and(ProfileSpecification.minAge(minAge))
                .and(ProfileSpecification.maxAge(maxAge))
                .and(ProfileSpecification.minGenderProbability(minGenderProb))
                .and(ProfileSpecification.minCountryProbability(minCountryProb));

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("page", page);
        result.put("limit", limit);
        result.put("total", profilePage.getTotalElements());
        result.put("data", profilePage.getContent().stream()
                .map(this::formatProfileList)
                .toList());

        return result;
    }

    // ==================== NATURAL LANGUAGE SEARCH ====================
    public Map<String, Object> searchProfiles(String query, int page, int limit) {
        QueryFilters filters = naturalLanguageParser.parse(query);

        if (filters == null || filters.isEmpty()) {
            return Map.of(
                    "status", "error",
                    "message", "Unable to interpret query"
            );
        }

        return getAllProfiles(
                filters.gender(), filters.ageGroup(), filters.countryId(),
                filters.minAge(), filters.maxAge(),
                filters.minGenderProb(), filters.minCountryProb(),
                "created_at", "desc", page, limit
        );
    }

    private @NotNull List<Profile> findByFilters(String gender, String countryId, String ageGroup) {
        boolean hasGender = hasValue(gender);
        boolean hasCountry = hasValue(countryId);
        boolean hasAgeGroup = hasValue(ageGroup);

        if (hasGender && hasCountry && hasAgeGroup) {
            return profileRepository.findByGenderIgnoreCaseAndCountryIdIgnoreCaseAndAgeGroupIgnoreCase(
                    gender, countryId, ageGroup);
        }
        if (hasGender && hasCountry) {
            return profileRepository.findByGenderIgnoreCaseAndCountryIdIgnoreCase(gender, countryId);
        }
        if (hasGender && hasAgeGroup) {
            return profileRepository.findByGenderIgnoreCaseAndAgeGroupIgnoreCase(gender, ageGroup);
        }
        if (hasCountry && hasAgeGroup) {
            return profileRepository.findByCountryIdIgnoreCaseAndAgeGroupIgnoreCase(countryId, ageGroup);
        }
        if (hasGender) return profileRepository.findByGenderIgnoreCase(gender);
        if (hasCountry) return profileRepository.findByCountryIdIgnoreCase(countryId);
        if (hasAgeGroup) return profileRepository.findByAgeGroupIgnoreCase(ageGroup);

        return profileRepository.findAll();
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isEmpty();
    }

    // ---------- DELETE ----------
    public void deleteProfile(UUID id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("NOT_FOUND: Profile not found");
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

    private int getInt(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key)).map(n -> ((Number) n).intValue()).orElse(0);
    }

    private double getDouble(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key)).map(n -> ((Number) n).doubleValue()).orElse(0.0);
    }

    private @NotNull Map<String, Object> formatProfile(@NotNull Profile p) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", p.getId().toString());
        data.put("name", p.getName());
        data.put("gender", p.getGender());
        data.put("gender_probability", p.getGenderProbability());
//        data.put("sample_size", p.getSampleSize());
        data.put("age", p.getAge());
        data.put("age_group", p.getAgeGroup());
        data.put("country_id", p.getCountryId());
        data.put("country_name", p.getCountryName());
        data.put("country_probability", p.getCountryProbability());
        data.put("created_at", p.getCreatedAt().toString());
        return data;
    }

//    private @NotNull Map<String, Object> formatProfileList(@NotNull Profile p) {
//        Map<String, Object> data = new LinkedHashMap<>();
//        data.put("id", p.getId().toString());
//        data.put("name", p.getName());
//        data.put("gender", p.getGender());
//        data.put("gender_probability", p.getGenderProbability()); // ADD THIS
//        data.put("age", p.getAge());
//        data.put("age_group", p.getAgeGroup());
//        data.put("country_id", p.getCountryId());
//        data.put("country_name", p.getCountryName());
//        data.put("country_probability", p.getCountryProbability()); // ADD THIS
//        data.put("created_at", p.getCreatedAt().toString()); // ADD THIS
//        return data;
//    }

    private @NotNull Map<String, Object> formatProfileList(@NotNull Profile p) {
        Map<String, Object> data = new LinkedHashMap<>();
        assert p.getId() != null;
        data.put("id", p.getId().toString());
        data.put("name", p.getName());
        data.put("gender", p.getGender());
        data.put("gender_probability", p.getGenderProbability());
        data.put("age", p.getAge());
        data.put("age_group", p.getAgeGroup());
        data.put("country_id", p.getCountryId());
        data.put("country_name", p.getCountryName());
        data.put("country_probability", p.getCountryProbability()); // ADD THIS
        data.put("created_at", p.getCreatedAt().toString());
        return data;
    }
}