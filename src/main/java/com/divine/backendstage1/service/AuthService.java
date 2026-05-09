package com.divine.backendstage1.service;

import com.divine.backendstage1.model.RefreshToken;
import com.divine.backendstage1.model.User;
import com.divine.backendstage1.repository.RefreshTokenRepository;
import com.divine.backendstage1.repository.UserRepository;
import com.fasterxml.uuid.Generators;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final RestClient restClient = RestClient.create();

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public String getGithubAuthUrl(String state, String codeChallenge) {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=read:user,user:email" +
                "&state=" + state +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";
    }

    @Transactional
    public Map<String, Object> handleCallback(String code, String codeVerifier) {
        // 1. Exchange code for GitHub access token
        // Build token request — use HashMap because Map.of() throws NPE on null values
        Map<String, String> tokenBody = new HashMap<>();
        tokenBody.put("client_id", clientId);
        tokenBody.put("client_secret", clientSecret);
        tokenBody.put("code", code);
        tokenBody.put("redirect_uri", redirectUri);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            tokenBody.put("code_verifier", codeVerifier);
        }

        Map<String, Object> tokenResponse = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .body(tokenBody)   // ← use the HashMap
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assert tokenResponse != null;
        String githubAccessToken = (String) tokenResponse.get("access_token");

        if (githubAccessToken == null) {
            throw new RuntimeException("GitHub token exchange failed: " +
                    tokenResponse.getOrDefault("error_description", tokenResponse.get("error")));
        }

        // 2. Get GitHub user info
        Map<String, Object> githubUser = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + githubAccessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assert githubUser != null;
        String githubId = String.valueOf(githubUser.get("id"));
        String username = (String) githubUser.get("login");
        String avatarUrl = (String) githubUser.get("avatar_url");
        String email = (String) githubUser.getOrDefault("email", "");

        // 3. Create or update user
        User user = userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(Generators.timeBasedEpochGenerator().generate());
            newUser.setGithubId(githubId);
            newUser.setRole("analyst");
            newUser.setActive(true);
            newUser.setCreatedAt(Instant.now());
            return newUser;
        });

        user.setUsername(username);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 4. Issue tokens
        return issueTokens(user);
    }

    @Transactional
    public Map<String, Object> refreshTokens(String oldToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new RuntimeException("UNAUTHORIZED: Invalid refresh token"));

        if (existing.isUsed()) {
            throw new RuntimeException("UNAUTHORIZED: Refresh token already used");
        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("UNAUTHORIZED: Refresh token expired");
        }

        // Invalidate old token
        existing.setUsed(true);
        refreshTokenRepository.save(existing);

        return issueTokens(existing.getUser());
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            refreshTokenRepository.save(t);
        });
    }

    private Map<String, Object> issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole());

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(rawRefreshToken);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiry));
        refreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return Map.of(
                "status", "success",
                "access_token", accessToken,
                "refresh_token", rawRefreshToken,
                "user", Map.of(
                        "id", user.getId().toString(),
                        "username", user.getUsername(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "avatar_url", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                        "role", user.getRole()
                )
        );
    }
}