package com.divine.backendstage1.controller;

import com.divine.backendstage1.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // 1. POST /api/profiles
    // ----- CREATE (for seeding / manual entry) -----
    @PostMapping
    public ResponseEntity<Object> createProfile(@RequestBody Map<String, Object> body) {

        // 400 - missing or empty name
        if (body == null || !body.containsKey("name") ||
                body.get("name") == null ||
                body.get("name").toString().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "message", "Missing or empty name"));
        }

        // 422 - name must be a string (not a number or object)
        if (!(body.get("name") instanceof String)) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("status", "error",
                            "message", "Invalid type"));
        }

        String name = body.get("name").toString().trim();

        try {
            Map<String, Object> result = profileService.createProfile(name);

            if ("exists".equals(result.get("status"))) {
                // return clean response with 200
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "data", result.get("data")
                ));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (RuntimeException e) {
            return handleException(e);
        }
    }




    // 2. GET /api/profiles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> getProfile(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Map<String, Object> result = profileService.getProfile(uuid);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "message", "Invalid UUID format"));
        } catch (RuntimeException e) {
            return handleException(e);
        }
    }


//    // 3. GET /api/profiles
//    @GetMapping
//    public ResponseEntity<Object> getAllProfiles(
//            @RequestParam(required = false) String gender,
//            @RequestParam(required = false) String country_id,
//            @RequestParam(required = false) String age_group) {
//        try {
//            Map<String, Object> result =
//                    profileService.getAllProfiles(gender, country_id, age_group);
//            return ResponseEntity.ok(result);
//        } catch (RuntimeException e) {
//            return handleException(e);
//        }
//    }

    // 3. GET /api/profiles (Advanced with filters, sorting, pagination)
    @GetMapping
    public ResponseEntity<Object> getAllProfiles(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String age_group,
            @RequestParam(required = false) String country_id,
            @RequestParam(required = false) Integer min_age,
            @RequestParam(required = false) Integer max_age,
            @RequestParam(required = false) Double min_gender_probability,
            @RequestParam(required = false) Double min_country_probability,
            @RequestParam(required = false, defaultValue = "created_at") String sort_by,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        // Validate parameters (basic)
//        if (page < 1) page = 1;
//        if (!order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) order = "desc";
        // Validate pagination
        if (page < 1) page = 1;
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50;

        try {
            // Validate sort_by
            if (!Set.of("age", "created_at", "gender_probability").contains(sort_by)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Invalid sort_by parameter"));
            }

            // Validate order
            if (!Set.of("asc", "desc").contains(order.toLowerCase())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Invalid order parameter"));
            }

            Map<String, Object> result = profileService.getAllProfiles(
                    gender, age_group, country_id,
                    min_age, max_age,
                    min_gender_probability, min_country_probability,
                    sort_by, order, page, limit);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    // 4. DELETE /api/profiles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProfile(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            profileService.deleteProfile(uuid);
//            return ResponseEntity.noContent().build(); // 204
            return ResponseEntity.ok(Map.of("status", "success", "message", "Profile deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "message", "Invalid UUID format"));
        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

//    // ----- DELETE -----
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> deleteProfile(@PathVariable UUID id) {
//        try {
//            profileService.deleteProfile(id);
//            return ResponseEntity.ok(Map.of("status", "success", "message", "Profile deleted"));
//        } catch (RuntimeException e) {
//            if (e.getMessage().contains("NOT_FOUND")) {
//                return ResponseEntity.status(404).body(Map.of(
//                        "status", "error",
//                        "message", "Profile not found"
//                ));
//            }
//            throw e;
//        }
//    }

    // ---------- SHARED ERROR HANDLER ----------
    private ResponseEntity<Object> handleException(RuntimeException e) {
        String message = e.getMessage();

        if (message != null && message.startsWith("NOT_FOUND:")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error",
                            "message", message.replace("NOT_FOUND: ", "")));
        }

        if (message != null && message.startsWith("UPSTREAM_ERROR:")) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("status", "error",
                            "message", message.replace("UPSTREAM_ERROR: ", "")));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error",
                        "message", "Internal server error"));
    }

    // ----- NATURAL LANGUAGE SEARCH -----
    //    // 4. GET /api/profiles/search (Natural Language Query)
//    @GetMapping("/search")
//    public ResponseEntity<Map<String, Object>> searchProfiles(
//            @RequestParam(required = false) String q,
//            @RequestParam(required = false, defaultValue = "1") int page,
//            @RequestParam(required = false, defaultValue = "10") int limit) {
//
//        if (q == null || q.isBlank()) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "status", "error",
//                    "message", "Query parameter 'q' is required"
//            ));
//        }
//
//        Map<String, Object> response = profileService.searchProfiles(q, page, limit);
//
//        if ("error".equals(response.get("status"))) {
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProfiles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Query parameter 'q' is required"
            ));
        }

        Map<String, Object> response = profileService.searchProfiles(q.trim(), page, limit);

        if ("error".equals(response.get("status"))) {
            return ResponseEntity.status(422).body(response);
        }

        return ResponseEntity.ok(response);
    }
}

