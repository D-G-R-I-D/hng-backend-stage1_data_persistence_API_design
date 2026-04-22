package com.divine.backendstage1.service;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NaturalLanguageParser {

    // Country name to ISO code mapping
    private static final Map<String, String> COUNTRY_MAP = new HashMap<>() {{
        put("nigeria", "NG");
        put("ghana", "GH");
        put("kenya", "KE");
        put("south africa", "ZA");
        put("ethiopia", "ET");
        put("angola", "AO");
        put("benin", "BJ");
        put("togo", "TG");
        put("ivory coast", "CI");
        put("cote d'ivoire", "CI");
        put("senegal", "SN");
        put("cameroon", "CM");
        put("uganda", "UG");
        put("rwanda", "RW");
        put("tanzania", "TZ");
        put("mozambique", "MZ");
        put("zambia", "ZM");
        put("zimbabwe", "ZW");
        put("malawi", "MW");
        put("botswana", "BW");
        put("namibia", "NA");
        put("mali", "ML");
        put("burkina faso", "BF");
        put("niger", "NE");
        put("chad", "TD");
        put("sudan", "SD");
        put("south sudan", "SS");
        put("eritrea", "ER");
        put("somalia", "SO");
        put("djibouti", "DJ");
        put("mauritania", "MR");
        put("gambia", "GM");
        put("guinea", "GN");
        put("guinea bissau", "GW");
        put("sierra leone", "SL");
        put("liberia", "LR");
        put("morocco", "MA");
        put("algeria", "DZ");
        put("tunisia", "TN");
        put("libya", "LY");
        put("egypt", "EG");
        put("usa", "US");
        put("united states", "US");
        put("united kingdom", "GB");
        put("uk", "GB");
        put("canada", "CA");
        put("australia", "AU");
        put("germany", "DE");
        put("france", "FR");
        put("italy", "IT");
        put("spain", "ES");
        put("portugal", "PT");
        put("netherlands", "NL");
        put("belgium", "BE");
        put("switzerland", "CH");
        put("sweden", "SE");
        put("norway", "NO");
        put("denmark", "DK");
        put("finland", "FI");
        put("poland", "PL");
        put("russia", "RU");
        put("china", "CN");
        put("japan", "JP");
        put("india", "IN");
        put("brazil", "BR");
        put("mexico", "MX");
        put("argentina", "AR");
        put("colombia", "CO");
        put("chile", "CL");
        put("peru", "PE");
    }};

    private static final Set<String> AGE_GROUPS = Set.of("child", "children", "teenager", "teenagers", "teen", "teens", "adult", "adults", "senior", "seniors", "elderly", "old");

    @Nullable
    public QueryFilters parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Remove filler words
        lowerQuery = removeFillerWords(lowerQuery);

        String gender = null;
        String ageGroup = null;
        String countryId = null;
        Integer minAge = null;
        Integer maxAge = null;

        // === 1. Detect Gender ===
        boolean b1 = lowerQuery.contains("female") || lowerQuery.contains("women") || lowerQuery.contains("woman") || lowerQuery.contains("girl") || lowerQuery.contains("girls");
        if (lowerQuery.contains("male") || lowerQuery.contains("men") || lowerQuery.contains("man") || lowerQuery.contains("boy") || lowerQuery.contains("boys")) {
            // Check if it's "male and female" or similar
            if (b1) {
                // Both genders - no filter
            } else {
                gender = "male";
            }
        } else if (b1) {
            gender = "female";
        }

        // === 2. Detect Age Group & Age Range ===
        // Check for specific age ranges
        Pattern agePattern = Pattern.compile("(?:above|over|older than|greater than|>)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
        Matcher ageMatcher = agePattern.matcher(lowerQuery);
        if (ageMatcher.find()) {
            minAge = Integer.parseInt(ageMatcher.group(1));
        }

        Pattern ageBelowPattern = Pattern.compile("(?:below|under|younger than|less than|<)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
        Matcher ageBelowMatcher = ageBelowPattern.matcher(lowerQuery);
        if (ageBelowMatcher.find()) {
            maxAge = Integer.parseInt(ageBelowMatcher.group(1)) - 1;
        }

        Pattern ageBetweenPattern = Pattern.compile("(?:between|aged?)\\s*(\\d+)\\s*(?:and|to|-)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
        Matcher ageBetweenMatcher = ageBetweenPattern.matcher(lowerQuery);
        if (ageBetweenMatcher.find()) {
            minAge = Integer.parseInt(ageBetweenMatcher.group(1));
            maxAge = Integer.parseInt(ageBetweenMatcher.group(2));
        }

        // Detect "young" keyword
        if (lowerQuery.contains("young") || lowerQuery.contains("youth") || lowerQuery.contains("youngster")  || lowerQuery.contains("youngish") || lowerQuery.contains("younger")) {
            // Young maps to ages 16-24 for parsing only
            if (minAge == null) minAge = 16;
            if (maxAge == null || maxAge > 24) maxAge = 24;
        }

        // Detect age group keywords
        if (containsAny(lowerQuery, "child", "children")) {
            ageGroup = "child";
            if (minAge == null) minAge = 0;
            if (maxAge == null) maxAge = 12;
        } else if (containsAny(lowerQuery, "teenager", "teenagers", "teen", "teens", "adolescent", "adolescents")) {
            ageGroup = "teenager";
            if (minAge == null) minAge = 13;
            if (maxAge == null) maxAge = 19;
        } else if (containsAny(lowerQuery, "adult", "adults", "grown", "grown-up")) {
            ageGroup = "adult";
            if (minAge == null) minAge = 20;
            if (maxAge == null) maxAge = 59;
        } else if (containsAny(lowerQuery, "senior", "seniors", "elderly", "old", "aged")) {
            ageGroup = "senior";
            if (minAge == null) minAge = 60;
        }

        // === 3. Detect Country ===
        for (Map.Entry<String, String> entry : COUNTRY_MAP.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                countryId = entry.getValue();
                break;
            }
        }

        // === 4. Detect "from" keyword for country ===
        Pattern fromPattern = Pattern.compile("from\\s+(\\w+(?:\\s+\\w+)?)");
        Matcher fromMatcher = fromPattern.matcher(lowerQuery);
        if (fromMatcher.find() && countryId == null) {
            String place = fromMatcher.group(1);
            for (Map.Entry<String, String> entry : COUNTRY_MAP.entrySet()) {
                if (place.contains(entry.getKey())) {
                    countryId = entry.getValue();
                    break;
                }
            }
        }

        // === 5. Detect "above/below X" patterns ===
        Pattern aboveAgePattern = Pattern.compile("above\\s+(\\d+)");
        Matcher aboveMatcher = aboveAgePattern.matcher(lowerQuery);
        if (aboveMatcher.find()) {
            minAge = Integer.parseInt(aboveMatcher.group(1));
        }

        Pattern belowAgePattern = Pattern.compile("below\\s+(\\d+)");
        Matcher belowMatcher = belowAgePattern.matcher(lowerQuery);
        if (belowMatcher.find()) {
            maxAge = Integer.parseInt(belowMatcher.group(1)) - 1;
        }

        // === Validate: If nothing found, return null ===
        if (gender == null && ageGroup == null && countryId == null
                && minAge == null && maxAge == null) {
            return null;
        }

        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
    }

    private String removeFillerWords(String query) {
        // Remove common filler words
        return query
                .replaceAll("\\b(i want|i need|give me|show me|find|get|fetch|list|display|all the|all|the|a|an|of|for|in|that|with|and|or)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}