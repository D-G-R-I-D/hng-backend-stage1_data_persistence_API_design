//package com.divine.backendstage1.service;
//
//import jakarta.annotation.Nullable;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class NaturalLanguageParser {
//
//    // Country name to ISO code mapping
//    private static final Map<String, String> COUNTRY_MAP = new HashMap<>() {{
//        put("nigeria", "NG");
//        put("ghana", "GH");
//        put("kenya", "KE");
//        put("south africa", "ZA");
//        put("ethiopia", "ET");
//        put("angola", "AO");
//        put("benin", "BJ");
//        put("togo", "TG");
//        put("ivory coast", "CI");
//        put("cote d'ivoire", "CI");
//        put("senegal", "SN");
//        put("cameroon", "CM");
//        put("uganda", "UG");
//        put("rwanda", "RW");
//        put("tanzania", "TZ");
//        put("mozambique", "MZ");
//        put("zambia", "ZM");
//        put("zimbabwe", "ZW");
//        put("malawi", "MW");
//        put("botswana", "BW");
//        put("namibia", "NA");
//        put("mali", "ML");
//        put("burkina faso", "BF");
//        put("niger", "NE");
//        put("chad", "TD");
//        put("sudan", "SD");
//        put("south sudan", "SS");
//        put("eritrea", "ER");
//        put("somalia", "SO");
//        put("djibouti", "DJ");
//        put("mauritania", "MR");
//        put("gambia", "GM");
//        put("guinea", "GN");
//        put("guinea bissau", "GW");
//        put("sierra leone", "SL");
//        put("liberia", "LR");
//        put("morocco", "MA");
//        put("algeria", "DZ");
//        put("tunisia", "TN");
//        put("libya", "LY");
//        put("egypt", "EG");
//        put("usa", "US");
//        put("united states", "US");
//        put("united kingdom", "GB");
//        put("uk", "GB");
//        put("canada", "CA");
//        put("australia", "AU");
//        put("germany", "DE");
//        put("france", "FR");
//        put("italy", "IT");
//        put("spain", "ES");
//        put("portugal", "PT");
//        put("netherlands", "NL");
//        put("belgium", "BE");
//        put("switzerland", "CH");
//        put("sweden", "SE");
//        put("norway", "NO");
//        put("denmark", "DK");
//        put("finland", "FI");
//        put("poland", "PL");
//        put("russia", "RU");
//        put("china", "CN");
//        put("japan", "JP");
//        put("india", "IN");
//        put("brazil", "BR");
//        put("mexico", "MX");
//        put("argentina", "AR");
//        put("colombia", "CO");
//        put("chile", "CL");
//        put("peru", "PE");
//        put("pakistan", "PK");
//        put("bangladesh", "BD");
//        put("indonesia", "ID");
//        put("philippines", "PH");
//        put("vietnam", "VN");
//        put("thailand", "TH");
//        put("south korea", "KR");
//        put("turkey", "TR");
//        put("saudi arabia", "SA");
//        put("united arab emirates", "AE");
//        put("malaysia", "MY");
//        put("ukraine", "UA");
//        put("czech republic", "CZ");
//        put("romania", "RO");
//        put("hungary", "HU");
//        put("greece", "GR");
//        put("austria", "AT");
//        put("ireland", "IE");
//        put("venezuela", "VE");
//        put("ecuador", "EC");
//        put("bolivia", "BO");
//        put("paraguay", "PY");
//        put("uruguay", "UY");
//        put("new zealand", "NZ");
//    }};
//
//    private static final Set<String> AGE_GROUPS = Set.of("child", "children", "teenager", "teenagers", "teen", "teens", "adult", "adults", "senior", "seniors", "elderly", "old");
//
////    @Nullable
////    public QueryFilters parse(String query) {
////        if (query == null || query.trim().isEmpty()) {
////            return null;
////        }
////
////        String lowerQuery = query.toLowerCase().trim();
////
////        // Remove filler words
////        lowerQuery = removeFillerWords(lowerQuery);
////
////        String gender = null;
////        String ageGroup = null;
////        String countryId = null;
////        Integer minAge = null;
////        Integer maxAge = null;
////
////        // === 1. Detect Gender ===
//////        boolean b1 = lowerQuery.contains("female") || lowerQuery.contains("women") || lowerQuery.contains("woman") || lowerQuery.contains("girl") || lowerQuery.contains("girls");
//////        if (lowerQuery.contains("male") || lowerQuery.contains("men") || lowerQuery.contains("man") || lowerQuery.contains("boy") || lowerQuery.contains("boys")) {
//////            // Check if it's "male and female" or similar
//////            if (b1) {
//////                // Both genders - no filter
//////            } else {
//////                gender = "male";
//////            }
//////        } else if (b1) {
//////            gender = "female";
//////        }
////
////        // === 1. Detect Gender ===
////        boolean hasFemale = lowerQuery.matches(".*\\b(female|females?|women|woman|girl|girls?)\\b.*");
////        boolean hasMale = lowerQuery.matches(".*\\b(male|males?|men|man|boy|boys?)\\b.*");
////
////        if (hasMale && hasFemale) {
////            // Both present - no gender filter
////        } else if (hasMale) {
////            gender = "male";
////        } else if (hasFemale) {
////            gender = "female";
////        }
////
////        // === 2. Detect Age Group & Age Range ===
////        // Check for specific age ranges
////        Pattern agePattern = Pattern.compile("(?:above|over|older than|greater than|>)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
////        Matcher ageMatcher = agePattern.matcher(lowerQuery);
////        if (ageMatcher.find()) {
////            minAge = Integer.parseInt(ageMatcher.group(1));
////        }
////
////        Pattern ageBelowPattern = Pattern.compile("(?:below|under|younger than|less than|<)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
////        Matcher ageBelowMatcher = ageBelowPattern.matcher(lowerQuery);
////        if (ageBelowMatcher.find()) {
////            maxAge = Integer.parseInt(ageBelowMatcher.group(1)) - 1;
////        }
////
////        Pattern ageBetweenPattern = Pattern.compile("(?:between|aged?)\\s*(\\d+)\\s*(?:and|to|-)\\s*(\\d+)(?:\\s*years?\\s*(?:old)?)?", Pattern.CASE_INSENSITIVE);
////        Matcher ageBetweenMatcher = ageBetweenPattern.matcher(lowerQuery);
////        if (ageBetweenMatcher.find()) {
////            minAge = Integer.parseInt(ageBetweenMatcher.group(1));
////            maxAge = Integer.parseInt(ageBetweenMatcher.group(2));
////        }
////
////        // Detect "young" keyword
////        if (lowerQuery.contains("young") || lowerQuery.contains("youth") || lowerQuery.contains("youngster")  || lowerQuery.contains("youngish") || lowerQuery.contains("younger")) {
////            // Young maps to ages 16-24 for parsing only
////            if (minAge == null) minAge = 16;
////            if (maxAge == null || maxAge > 24) maxAge = 24;
////        }
////
////        // Detect age group keywords
////        if (containsAny(lowerQuery, "child", "children")) {
////            ageGroup = "child";
////            if (minAge == null) minAge = 0;
////            if (maxAge == null) maxAge = 12;
////        } else if (containsAny(lowerQuery, "teenager", "teenagers", "teen", "teens", "adolescent", "adolescents")) {
////            ageGroup = "teenager";
////            if (minAge == null) minAge = 13;
////            if (maxAge == null) maxAge = 19;
////        } else if (containsAny(lowerQuery, "adult", "adults", "grown", "grown-up")) {
////            ageGroup = "adult";
////            if (minAge == null) minAge = 20;
////            if (maxAge == null) maxAge = 59;
////        } else if (containsAny(lowerQuery, "senior", "seniors", "elderly", "old", "aged")) {
////            ageGroup = "senior";
////            if (minAge == null) minAge = 60;
////        }
////
////        // === 3. Detect Country ===
////
////        // === 3. Detect Country ===
////        // Sort by key length descending so "nigeria" matches before "niger"
////        String finalLowerQuery = lowerQuery;
////        countryId = COUNTRY_MAP.entrySet().stream()
////                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
////                .filter(entry -> finalLowerQuery.contains(entry.getKey()))
////                .map(Map.Entry::getValue)
////                .findFirst()
////                .orElse(null);
//////        for (Map.Entry<String, String> entry : COUNTRY_MAP.entrySet()) {
//////            if (lowerQuery.contains(entry.getKey())) {
//////                countryId = entry.getValue();
//////                break;
//////            }
//////        }
////
//////        // === 4. Detect "from" keyword for country ===
//////        Pattern fromPattern = Pattern.compile("from\\s+(\\w+(?:\\s+\\w+)?)");
//////        Matcher fromMatcher = fromPattern.matcher(lowerQuery);
//////        if (fromMatcher.find() && countryId == null) {
//////            String place = fromMatcher.group(1);
//////            for (Map.Entry<String, String> entry : COUNTRY_MAP.entrySet()) {
//////                if (place.contains(entry.getKey())) {
//////                    countryId = entry.getValue();
//////                    break;
//////                }
//////            }
//////        }
////
////        // === 4. Detect "from" keyword ===
////        Pattern fromPattern = Pattern.compile("from\\s+(\\w+(?:\\s+\\w+)?)");
////        Matcher fromMatcher = fromPattern.matcher(lowerQuery);
////        if (fromMatcher.find() && countryId == null) {
////            String place = fromMatcher.group(1);
////            countryId = COUNTRY_MAP.entrySet().stream()
////                    .sorted((a, b) -> b.getKey().length() - a.getKey().length())
////                    .filter(entry -> place.contains(entry.getKey()))
////                    .map(Map.Entry::getValue)
////                    .findFirst()
////                    .orElse(null);
////        }
////
////        // === 5. Detect "above/below X" patterns ===
////        Pattern aboveAgePattern = Pattern.compile("above\\s+(\\d+)");
////        Matcher aboveMatcher = aboveAgePattern.matcher(lowerQuery);
////        if (aboveMatcher.find()) {
////            minAge = Integer.parseInt(aboveMatcher.group(1));
////        }
////
////        Pattern belowAgePattern = Pattern.compile("below\\s+(\\d+)");
////        Matcher belowMatcher = belowAgePattern.matcher(lowerQuery);
////        if (belowMatcher.find()) {
////            maxAge = Integer.parseInt(belowMatcher.group(1)) - 1;
////        }
////
////        // === Validate: If nothing found, return null ===
////        if (gender == null && ageGroup == null && countryId == null
////                && minAge == null && maxAge == null) {
////            return null;
////        }
////
////        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
////    }
//
//
////    @Nullable
////    public QueryFilters parse(String query) {
////        if (query == null || query.trim().isEmpty()) return null;
////
////        String lowerQuery = query.toLowerCase().trim();
////
////        // === 1. Gender — before filler removal, with plurals ===
////        boolean hasFemale = lowerQuery.matches(".*\\b(females?|women|woman|girls?)\\b.*");
////        boolean hasMale = lowerQuery.matches(".*\\b(males?|men|man|boys?)\\b.*");
////
////        String gender = null;
////        if (hasMale && !hasFemale) gender = "male";
////        else if (hasFemale && !hasMale) gender = "female";
////
////        // === 2. Age ranges — before filler removal ===
////        Integer minAge = null;
////        Integer maxAge = null;
////
////        Matcher betweenMatcher = Pattern.compile(
////                "(?:between|aged?)\\s*(\\d+)\\s*(?:and|to|-)\\s*(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(lowerQuery);
////        if (betweenMatcher.find()) {
////            minAge = Integer.parseInt(betweenMatcher.group(1));
////            maxAge = Integer.parseInt(betweenMatcher.group(2));
////        }
////
////        Matcher aboveMatcher = Pattern.compile(
////                "(?:above|over|older than|greater than|>)\\s*(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(lowerQuery);
////        if (aboveMatcher.find() && minAge == null) {
////            minAge = Integer.parseInt(aboveMatcher.group(1));
////        }
////
////        Matcher belowMatcher = Pattern.compile(
////                "(?:below|under|younger than|less than|<)\\s*(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(lowerQuery);
////        if (belowMatcher.find() && maxAge == null) {
////            maxAge = Integer.parseInt(belowMatcher.group(1)) - 1;
////        }
////
////        if (lowerQuery.matches(".*\\b(young|youth|youngster|youngish|younger)\\b.*")) {
////            if (minAge == null) minAge = 16;
////            if (maxAge == null) maxAge = 24;
////        }
////
////        // === 3. Age group ===
////        String ageGroup = null;
////        if (lowerQuery.matches(".*\\b(child|children)\\b.*")) {
////            ageGroup = "child";
////            if (minAge == null) minAge = 0;
////            if (maxAge == null) maxAge = 12;
////        } else if (lowerQuery.matches(".*\\b(teens?|teenagers?|adolescents?)\\b.*")) {
////            ageGroup = "teenager";
////            if (minAge == null) minAge = 13;
////            if (maxAge == null) maxAge = 19;
////        } else if (lowerQuery.matches(".*\\b(adults?|grown.?ups?)\\b.*")) {
////            ageGroup = "adult";
////            if (minAge == null) minAge = 20;
////            if (maxAge == null) maxAge = 59;
////        } else if (lowerQuery.matches(".*\\b(seniors?|elderly|aged)\\b.*")) {
////            ageGroup = "senior";
////            if (minAge == null) minAge = 60;
////        }
////
////        // === 4. Country — longest match first ===
////        String countryId = COUNTRY_MAP.entrySet().stream()
////                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
////                .filter(entry -> lowerQuery.contains(entry.getKey()))
////                .map(Map.Entry::getValue)
////                .findFirst()
////                .orElse(null);
////
////        // === 5. Nothing found ===
////        if (gender == null && ageGroup == null && countryId == null
////                && minAge == null && maxAge == null) {
////            return null;
////        }
////
////        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
////    }
//
//    @NotNull
//    public QueryFilters parse(String query) {
//        if (query == null || query.trim().isEmpty()) return null;
//
//        String q = query.toLowerCase().trim();
//
//        // ── 1. GENDER ────────────────────────────────────────────────────────
//        // Strip female words first, then check for male words in what remains
//        // This prevents "female" from being counted as containing "male"
//
//        boolean hasFemale = Pattern.compile(
//                        "\\b(female|females|woman|women|girl|girls)\\b")
//                .matcher(q).find();
//
//        // Remove all female-related words before checking for male
//        String qStrippedFemale = q
//                .replaceAll("\\bfemales?\\b", "REMOVED")
//                .replaceAll("\\bwom[ae]n\\b", "REMOVED")
//                .replaceAll("\\bgirls?\\b", "REMOVED");
//
//        boolean hasMale = Pattern.compile(
//                        "\\b(male|males|man|men|boy|boys)\\b")
//                .matcher(qStrippedFemale).find();
//
//        String gender = null;
//        if (hasMale && hasFemale) {
//            gender = null; // both → no filter
//        } else if (hasMale) {
//            gender = "male";
//        } else if (hasFemale) {
//            gender = "female";
//        }
//
//        // ── 2. AGE RANGES ────────────────────────────────────────────────────
//        Integer minAge = null;
//        Integer maxAge = null;
//
//        // "between X and Y"
//        Matcher betweenMatcher = Pattern.compile(
//                "between\\s+(\\d+)\\s+and\\s+(\\d+)",
//                Pattern.CASE_INSENSITIVE).matcher(q);
//        if (betweenMatcher.find()) {
//            minAge = Integer.parseInt(betweenMatcher.group(1));
//            maxAge = Integer.parseInt(betweenMatcher.group(2));
//        }
//
//        // "above X" / "over X" / "older than X" / "greater than X"
//        if (minAge == null) {
//            Matcher aboveMatcher = Pattern.compile(
//                    "\\b(?:above|over|older\\s+than|greater\\s+than)\\s+(\\d+)",
//                    Pattern.CASE_INSENSITIVE).matcher(q);
//            if (aboveMatcher.find()) {
//                minAge = Integer.parseInt(aboveMatcher.group(1));
//            }
//        }
//
//        // "below X" / "under X" / "younger than X" / "less than X"
//        if (maxAge == null) {
//            Matcher belowMatcher = Pattern.compile(
//                    "\\b(?:below|under|younger\\s+than|less\\s+than)\\s+(\\d+)",
//                    Pattern.CASE_INSENSITIVE).matcher(q);
//            if (belowMatcher.find()) {
//                maxAge = Integer.parseInt(belowMatcher.group(1));
//            }
//        }
//
//        // "young" → 16–24 only if no other range set
//        if (Pattern.compile("\\byoung\\b").matcher(q).find()
//                && minAge == null && maxAge == null) {
//            minAge = 16;
//            maxAge = 24;
//        }
//
//        // ── 3. AGE GROUP ─────────────────────────────────────────────────────
//        String ageGroup = null;
//        if (Pattern.compile("\\b(child|children|kid|kids)\\b").matcher(q).find()) {
//            ageGroup = "child";
//        } else if (Pattern.compile("\\b(teen|teens|teenager|teenagers|adolescent)\\b")
//                .matcher(q).find()) {
//            ageGroup = "teenager";
//        } else if (Pattern.compile("\\b(adult|adults)\\b").matcher(q).find()) {
//            ageGroup = "adult";
//        } else if (Pattern.compile("\\b(senior|seniors|elderly)\\b").matcher(q).find()) {
//            ageGroup = "senior";
//        }
//
//        // ── 4. COUNTRY — longest match first ─────────────────────────────────
//        String countryId = COUNTRY_MAP.entrySet().stream()
//                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
//                .filter(e -> Pattern.compile(
//                        "\\b" + Pattern.quote(e.getKey()) + "\\b",
//                        Pattern.CASE_INSENSITIVE).matcher(q).find())
//                .map(Map.Entry::getValue)
//                .findFirst()
//                .orElse(null);
//
//        // ── 5. Nothing recognized ─────────────────────────────────────────────
//        if (gender == null && ageGroup == null && countryId == null
//                && minAge == null && maxAge == null) {
//            return null;
//        }
//
//        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
//    }
////    public QueryFilters parse(String query) {
////        if (query == null || query.trim().isEmpty()) return null;
////
////        String q = query.toLowerCase().trim();
////
////        // ── 1. GENDER ────────────────────────────────────────────────────
////        // Check female words FIRST and explicitly
////        // Use strict patterns that won't cross-match
////        boolean hasFemale = containsWord(q, "female")
////                || containsWord(q, "females")
////                || containsWord(q, "woman")
////                || containsWord(q, "women")
////                || containsWord(q, "girl")
////                || containsWord(q, "girls");
////
////        boolean hasMaleWord = containsWord(q, "male")
////                || containsWord(q, "males")
////                || containsWord(q, "man")
////                || containsWord(q, "men")
////                || containsWord(q, "boy")
////                || containsWord(q, "boys");
////
////        // "male" inside "female" should NOT count as male
////        // We need to strip "female/females" from query before checking male
////        String qWithoutFemale = q
////                .replaceAll("\\bfemales?\\b", "")
////                .replaceAll("\\bwom[ae]n\\b", "")
////                .replaceAll("\\bgirls?\\b", "")
////                .trim();
////
////        boolean hasMale = containsWord(qWithoutFemale, "male")
////                || containsWord(qWithoutFemale, "males")
////                || containsWord(qWithoutFemale, "man")
////                || containsWord(qWithoutFemale, "men")
////                || containsWord(qWithoutFemale, "boy")
////                || containsWord(qWithoutFemale, "boys");
////
////        String gender = null;
////        if (hasMale && hasFemale) {
////            gender = null; // both → no filter
////        } else if (hasMale) {
////            gender = "male";
////        } else if (hasFemale) {
////            gender = "female";
////        }
////
////        // ── 2. AGE RANGES ─────────────────────────────────────────────────
////        Integer minAge = null;
////        Integer maxAge = null;
////
////        // "between X and Y"
////        Matcher betweenMatcher = Pattern.compile(
////                "between\\s+(\\d+)\\s+and\\s+(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(q);
////        if (betweenMatcher.find()) {
////            minAge = Integer.parseInt(betweenMatcher.group(1));
////            maxAge = Integer.parseInt(betweenMatcher.group(2));
////        }
////
////        // "above X" / "over X" / "older than X" / "greater than X"
////        if (minAge == null) {
////            Matcher aboveMatcher = Pattern.compile(
////                    "(?:above|over|older\\s+than|greater\\s+than)\\s+(\\d+)",
////                    Pattern.CASE_INSENSITIVE).matcher(q);
////            if (aboveMatcher.find()) {
////                minAge = Integer.parseInt(aboveMatcher.group(1));
////            }
////        }
////
////        // "below X" / "under X" / "younger than X" / "less than X"
////        if (maxAge == null) {
////            Matcher belowMatcher = Pattern.compile(
////                    "(?:below|under|younger\\s+than|less\\s+than)\\s+(\\d+)",
////                    Pattern.CASE_INSENSITIVE).matcher(q);
////            if (belowMatcher.find()) {
////                maxAge = Integer.parseInt(belowMatcher.group(1));
////            }
////        }
////
////        // "young" → ages 16-24 (only if no explicit age range given)
////        if (containsWord(q, "young") && minAge == null && maxAge == null) {
////            minAge = 16;
////            maxAge = 24;
////        }
////
////        // ── 3. AGE GROUP ──────────────────────────────────────────────────
////        String ageGroup = null;
////        if (containsWord(q, "child") || containsWord(q, "children")
////                || containsWord(q, "kid") || containsWord(q, "kids")) {
////            ageGroup = "child";
////        } else if (containsWord(q, "teenager") || containsWord(q, "teenagers")
////                || containsWord(q, "teen") || containsWord(q, "teens")
////                || containsWord(q, "adolescent")) {
////            ageGroup = "teenager";
////        } else if (containsWord(q, "adult") || containsWord(q, "adults")) {
////            ageGroup = "adult";
////        } else if (containsWord(q, "senior") || containsWord(q, "seniors")
////                || containsWord(q, "elderly")) {
////            ageGroup = "senior";
////        }
////
////        // ── 4. COUNTRY — longest match first ──────────────────────────────
////        String countryId = COUNTRY_MAP.entrySet().stream()
////                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
////                .filter(e -> containsPhrase(q, e.getKey()))
////                .map(Map.Entry::getValue)
////                .findFirst()
////                .orElse(null);
////
////        // ── 5. Nothing recognized → return null ───────────────────────────
////        if (gender == null && ageGroup == null && countryId == null
////                && minAge == null && maxAge == null) {
////            return null;
////        }
////
////        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
////    }
//
//    /**
//     * Check if a single word exists with word boundaries.
//     * Handles edge case where "male" is inside "female".
//     */
//    private boolean containsWord(String text, String word) {
//        Pattern p = Pattern.compile(
//                "(?<![a-z])" + Pattern.quote(word) + "(?![a-z])",
//                Pattern.CASE_INSENSITIVE);
//        return p.matcher(text).find();
//    }
//
//    /**
//     * Check if a phrase (possibly multi-word) exists in text.
//     * Uses word boundary on start and end.
//     */
//    private boolean containsPhrase(String text, String phrase) {
//        Pattern p = Pattern.compile(
//                "\\b" + Pattern.quote(phrase) + "\\b",
//                Pattern.CASE_INSENSITIVE);
//        return p.matcher(text).find();
//    }
////    public QueryFilters parse(String query) {
////        if (query == null || query.trim().isEmpty()) return null;
////
////        String q = query.toLowerCase().trim();
////
////        // ── 1. GENDER ─────────────────────────────────────────────────────
////        // Use strict word boundaries - check female FIRST to avoid
////        // "female" being caught by male pattern
////        boolean hasFemale = q.matches(".*\\b(female|females|women|woman|girl|girls)\\b.*");
////        // For male, explicitly exclude "female" matches
////        boolean hasMale = q.matches(".*\\b(male|males|men|man|boy|boys)\\b.*") && !hasFemale;
////
////        // Handle "male and female" → no gender filter
////        boolean hasBoth = q.matches(".*\\b(male|males)\\b.*")
////                && q.matches(".*\\b(female|females)\\b.*");
////
////        String gender = null;
////        if (hasBoth) {
////            gender = null;
////        } else if (hasMale) {
////            gender = "male";
////        } else if (hasFemale) {
////            gender = "female";
////        }
////
////        // ── 2. AGE RANGES ────────────────────────────────────────────────
////        Integer minAge = null;
////        Integer maxAge = null;
////
////        // "between X and Y"
////        Matcher betweenMatcher = Pattern.compile(
////                "between\\s+(\\d+)\\s+and\\s+(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(q);
////        if (betweenMatcher.find()) {
////            minAge = Integer.parseInt(betweenMatcher.group(1));
////            maxAge = Integer.parseInt(betweenMatcher.group(2));
////        }
////
////        // "above X" / "over X" / "older than X" / "greater than X"
////        Matcher aboveMatcher = Pattern.compile(
////                "(?:above|over|older than|greater than)\\s+(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(q);
////        if (aboveMatcher.find() && minAge == null) {
////            minAge = Integer.parseInt(aboveMatcher.group(1));
////        }
////
////        // "below X" / "under X" / "younger than X" / "less than X"
////        Matcher belowMatcher = Pattern.compile(
////                "(?:below|under|younger than|less than)\\s+(\\d+)",
////                Pattern.CASE_INSENSITIVE).matcher(q);
////        if (belowMatcher.find() && maxAge == null) {
////            maxAge = Integer.parseInt(belowMatcher.group(1));
////        }
////
////        // "young" → 16-24 (only if no other age range set)
////        if (q.matches(".*\\byoung\\b.*") && minAge == null && maxAge == null) {
////            minAge = 16;
////            maxAge = 24;
////        }
////
////        // ── 3. AGE GROUP ─────────────────────────────────────────────────
////        String ageGroup = null;
////        if (q.matches(".*\\b(child|children|kid|kids)\\b.*")) {
////            ageGroup = "child";
////        } else if (q.matches(".*\\b(teen|teens|teenager|teenagers|adolescent)\\b.*")) {
////            ageGroup = "teenager";
////        } else if (q.matches(".*\\b(adult|adults)\\b.*")) {
////            ageGroup = "adult";
////        } else if (q.matches(".*\\b(senior|seniors|elderly)\\b.*")) {
////            ageGroup = "senior";
////        }
////
////        // ── 4. COUNTRY — longest match wins ──────────────────────────────
////        String countryId = COUNTRY_MAP.entrySet().stream()
////                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
////                .filter(e -> {
////                    // Use word boundary check
////                    Pattern p = Pattern.compile(
////                            "\\b" + Pattern.quote(e.getKey()) + "\\b");
////                    return p.matcher(q).find();
////                })
////                .map(Map.Entry::getValue)
////                .findFirst()
////                .orElse(null);
////
////        // ── 5. RECOGNIZABLE? ─────────────────────────────────────────────
////        if (gender == null && ageGroup == null && countryId == null
////                && minAge == null && maxAge == null) {
////            return null;
////        }
////
////        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
////    }
//
//
//
//    private String removeFillerWords(String query) {
//        // Remove common filler words
//        return query
//                .replaceAll("\\b(i want|i need|give me|show me|find|get|fetch|list|display|all the|all|the|a|an|of|for|in|that|with|or)\\b", " ")
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//
//    private boolean containsAny(String text, String... words) {
//        for (String word : words) {
//            if (text.contains(word)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}


package com.divine.backendstage1.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NaturalLanguageParser {

    private static final Map<String, String> COUNTRY_MAP = Map.ofEntries(
            Map.entry("nigeria", "NG"),
            Map.entry("ghana", "GH"),
            Map.entry("kenya", "KE"),
            Map.entry("south africa", "ZA"),
            Map.entry("ethiopia", "ET"),
            Map.entry("angola", "AO"),
            Map.entry("benin", "BJ"),
            Map.entry("togo", "TG"),
            Map.entry("ivory coast", "CI"),
            Map.entry("cote d'ivoire", "CI"),
            Map.entry("senegal", "SN"),
            Map.entry("cameroon", "CM"),
            Map.entry("uganda", "UG"),
            Map.entry("rwanda", "RW"),
            Map.entry("tanzania", "TZ"),
            Map.entry("mozambique", "MZ"),
            Map.entry("zambia", "ZM"),
            Map.entry("zimbabwe", "ZW"),
            Map.entry("malawi", "MW"),
            Map.entry("botswana", "BW"),
            Map.entry("namibia", "NA"),
            Map.entry("mali", "ML"),
            Map.entry("burkina faso", "BF"),
            Map.entry("niger", "NE"),
            Map.entry("chad", "TD"),
            Map.entry("sudan", "SD"),
            Map.entry("south sudan", "SS"),
            Map.entry("eritrea", "ER"),
            Map.entry("somalia", "SO"),
            Map.entry("djibouti", "DJ"),
            Map.entry("mauritania", "MR"),
            Map.entry("gambia", "GM"),
            Map.entry("guinea", "GN"),
            Map.entry("guinea bissau", "GW"),
            Map.entry("sierra leone", "SL"),
            Map.entry("liberia", "LR"),
            Map.entry("morocco", "MA"),
            Map.entry("algeria", "DZ"),
            Map.entry("tunisia", "TN"),
            Map.entry("egypt", "EG"),
            Map.entry("libya", "LY"),
            Map.entry("usa", "US"),
            Map.entry("united states", "US"),
            Map.entry("united kingdom", "GB"),
            Map.entry("uk", "GB"),
            Map.entry("canada", "CA"),
            Map.entry("australia", "AU"),
            Map.entry("germany", "DE"),
            Map.entry("france", "FR"),
            Map.entry("italy", "IT"),
            Map.entry("spain", "ES"),
            Map.entry("portugal", "PT"),
            Map.entry("netherlands", "NL"),
            Map.entry("belgium", "BE"),
            Map.entry("sweden", "SE"),
            Map.entry("norway", "NO"),
            Map.entry("denmark", "DK"),
            Map.entry("finland", "FI"),
            Map.entry("poland", "PL"),
            Map.entry("russia", "RU"),
            Map.entry("china", "CN"),
            Map.entry("japan", "JP"),
            Map.entry("india", "IN"),
            Map.entry("brazil", "BR"),
            Map.entry("mexico", "MX"),
            Map.entry("argentina", "AR"),
            Map.entry("colombia", "CO"),
            Map.entry("chile", "CL"),
            Map.entry("peru", "PE"),
            Map.entry("venezuela", "VE"),
            Map.entry("south korea", "KR"),
            Map.entry("turkey", "TR"),
            Map.entry("saudi arabia", "SA"),
            Map.entry("malaysia", "MY"),
            Map.entry("new zealand", "NZ")
    );

    public QueryFilters parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String q = query.toLowerCase().trim();

        // ── GENDER ─────────────────────────────────────────
        boolean hasFemale = Pattern.compile("\\b(female|females|woman|women|girl|girls)\\b")
                .matcher(q).find();

        String qNoFemale = q.replaceAll("\\b(female|females|woman|women|girl|girls)\\b", " ");
        boolean hasMale = Pattern.compile("\\b(male|males|man|men|boy|boys)\\b")
                .matcher(qNoFemale).find();

        String gender = null;
        if (hasMale && hasFemale) {
            gender = null;
        } else if (hasMale) {
            gender = "male";
        } else if (hasFemale) {
            gender = "female";
        }

        // ── AGE ────────────────────────────────────────────
        Integer minAge = null;
        Integer maxAge = null;

        Matcher between = Pattern.compile("between\\s+(\\d+)\\s+and\\s+(\\d+)", Pattern.CASE_INSENSITIVE)
                .matcher(q);
        if (between.find()) {
            minAge = Integer.parseInt(between.group(1));
            maxAge = Integer.parseInt(between.group(2));
        }

        if (minAge == null) {
            Matcher above = Pattern.compile("(?:above|over|older than|greater than)\\s+(\\d+)", Pattern.CASE_INSENSITIVE)
                    .matcher(q);
            if (above.find()) {
                minAge = Integer.parseInt(above.group(1));
            }
        }

        if (maxAge == null) {
            Matcher below = Pattern.compile("(?:below|under|younger than|less than)\\s+(\\d+)", Pattern.CASE_INSENSITIVE)
                    .matcher(q);
            if (below.find()) {
                maxAge = Integer.parseInt(below.group(1));
            }
        }

        if (minAge == null && maxAge == null && Pattern.compile("\\byoung\\b").matcher(q).find()) {
            minAge = 16;
            maxAge = 24;
        }

        // ── AGE GROUP ──────────────────────────────────────
        String ageGroup = null;
        if (Pattern.compile("\\b(child|children|kid|kids)\\b").matcher(q).find()) {
            ageGroup = "child";
        } else if (Pattern.compile("\\b(teen|teens|teenager|teenagers|adolescent)\\b").matcher(q).find()) {
            ageGroup = "teenager";
        } else if (Pattern.compile("\\b(adult|adults)\\b").matcher(q).find()) {
            ageGroup = "adult";
        } else if (Pattern.compile("\\b(senior|seniors|elderly)\\b").matcher(q).find()) {
            ageGroup = "senior";
        }

        // ── COUNTRY ────────────────────────────────────────
        String countryId = COUNTRY_MAP.entrySet().stream()
                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
                .filter(e -> Pattern.compile("\\b" + Pattern.quote(e.getKey()) + "\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(q).find())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        // ── VALIDATE ───────────────────────────────────────
        if (gender == null && ageGroup == null && countryId == null && minAge == null && maxAge == null) {
            return null;
        }

        return new QueryFilters(gender, ageGroup, countryId, minAge, maxAge, null, null);
    }
}