package com.divine.backendstage1.service;

public record QueryFilters(
        String gender,
        String ageGroup,
        String countryId,
        Integer minAge,
        Integer maxAge,
        Double minGenderProb,
        Double minCountryProb
) {
    public boolean isEmpty() {
        return gender == null && ageGroup == null && countryId == null
                && minAge == null && maxAge == null
                && minGenderProb == null && minCountryProb == null;
    }
}