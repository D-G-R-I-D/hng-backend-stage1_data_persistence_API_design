package com.divine.backendstage1.controller;

import com.divine.backendstage1.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // 1. POST /api/profiles
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

            // If profile already exists return 200
//            if (result.containsKey("message")) {
//                return ResponseEntity.ok(result);
//            }
            // If profile already exists return 201 (idempotent)
//            if (result.containsKey("message")) {
//                return ResponseEntity.status(HttpStatus.CREATED).body(result);
//            }

            // New profile created return 201
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

    // 3. GET /api/profiles
    @GetMapping
    public ResponseEntity<Object> getAllProfiles(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String country_id,
            @RequestParam(required = false) String age_group) {
        try {
            Map<String, Object> result =
                    profileService.getAllProfiles(gender, country_id, age_group);
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
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "message", "Invalid UUID format"));
        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

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
}