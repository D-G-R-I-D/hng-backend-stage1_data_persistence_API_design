package com.divine.backendstage1.repository;

import com.divine.backendstage1.model.Profile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

public class ProfileSpecification {

    @Contract(pure = true)
    public static @NotNull Specification<Profile> hasGender(String gender) {
        return (root, query, cb) ->
                (gender == null || gender.isBlank() || gender.equals("unknown")) ? null
                        : cb.equal(cb.lower(root.get("gender")), gender.toLowerCase());
    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> hasAgeGroup(String ageGroup) {
        return (root, query, cb) -> ageGroup == null || ageGroup.isBlank() ? null
                : cb.equal(cb.lower(root.get("ageGroup")), ageGroup.toLowerCase());
    }

//    public static Specification<Profile> hasCountryId(String countryId) {
//        return (root, query, cb) ->
//                (countryId == null || countryId.isBlank() || countryId.equals("unknown")) ? null
//                        : cb.equal(cb.lower(root.get("countryId")), countryId.toLowerCase());
//    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> hasCountryId(String countryId) {
        return (root, query, cb) -> {
            if (countryId == null || countryId.isBlank() || countryId.equals("unknown")) return null;
            // stored as uppercase e.g. "NG", compare uppercase
            return cb.equal(
                    cb.upper(root.get("countryId")),
                    countryId.toUpperCase()
            );
        };
    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> minAge(Integer minAge) {
        return (root, query, cb) -> minAge == null ? null
                : cb.greaterThanOrEqualTo(root.get("age"), minAge);
    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> maxAge(Integer maxAge) {
        return (root, query, cb) -> maxAge == null ? null
                : cb.lessThanOrEqualTo(root.get("age"), maxAge);
    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> minGenderProbability(Double min) {
        return (root, query, cb) -> min == null ? null
                : cb.greaterThanOrEqualTo(root.get("genderProbability"), min);
    }

    @Contract(pure = true)
    public static @NotNull Specification<Profile> minCountryProbability(Double min) {
        return (root, query, cb) -> min == null ? null
                : cb.greaterThanOrEqualTo(root.get("countryProbability"), min);
    }
}