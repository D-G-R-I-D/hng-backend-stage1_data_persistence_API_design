package com.divine.backendstage1.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Service
public class ExternalApiService {

    private final RestClient restClient = RestClient.create();

    // --- Individual API calls ---

    public Map<String, Object> callGenderize(String name) {
        return restClient.get()
                .uri("""
                        https://api.genderize.io/?name={name}""", name)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("API Rate limit or bad request!");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Genderize server is down!");
                })
                .body(new ParameterizedTypeReference<>() {
                } );
    }

    public Map<String, Object> callAgify(String name) {
        return restClient.get()
                .uri("""
                        https://api.agify.io/?name={name}""", name)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("API Rate limit or bad request!");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Agify server is down!");
                })
                .body(new ParameterizedTypeReference<>() {
                } );
    }

    public Map<String, Object> callNationalize(String name) {
        return restClient.get()
                .uri("""
                        https://api.nationalize.io/?name={name}""", name)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("API Rate limit or bad request!");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Nationalize server is down!");
                })
                .body(new ParameterizedTypeReference<>() {
                } );
    }

    // --- Call all 3 APIs in parallel ---
    public CompletableFuture<Map<String, Object>> callGenderizeAsync(String name) {
        return CompletableFuture.supplyAsync(() -> callGenderize(name));
    }

    public CompletableFuture<Map<String, Object>> callAgifyAsync(String name) {
        return CompletableFuture.supplyAsync(() -> callAgify(name));
    }

    public CompletableFuture<Map<String, Object>> callNationalizeAsync(String name) {
        return CompletableFuture.supplyAsync(() -> callNationalize(name));
    }
}