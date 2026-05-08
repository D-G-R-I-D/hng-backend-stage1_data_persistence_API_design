package com.divine.backendstage1.controller;

import com.divine.backendstage1.model.User;
import com.divine.backendstage1.service.AuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // GET /auth/GitHub — redirect to GitHub
    @GetMapping("/github")
    public ResponseEntity<Void> githubLogin(
            @RequestParam String state,
            @RequestParam("code_challenge") String codeChallenge) {

        String url = authService.getGithubAuthUrl(state, codeChallenge);
        return ResponseEntity.status(302)
                .header("Location", url)
                .build();
    }

    // GET /auth/GitHub/callback — GitHub redirects here
    @GetMapping("/github/callback")
    public ResponseEntity<Object> githubCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier) {

        try {
            Map<String, Object> result = authService.handleCallback(code, codeVerifier);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of("status", "error",
                            "message", "GitHub authentication failed"));
        }
    }

    // POST /auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody @NotNull Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "refresh_token is required"));
        }
        try {
            return ResponseEntity.ok(authService.refreshTokens(refreshToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", "error",
                            "message", e.getMessage().replace("UNAUTHORIZED: ", "")));
        }
    }

    // POST /auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestBody @NotNull Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok(Map.of("status", "success", "message", "Logged out"));
    }

    // GET /auth/me — who am I
    @GetMapping("/me")
    public ResponseEntity<Object> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", "error", "message", "Not authenticated"));
        }
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of(
                        "id", user.getId().toString(),
                        "username", user.getUsername(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "avatar_url", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                        "role", user.getRole()
                )
        ));
    }
}