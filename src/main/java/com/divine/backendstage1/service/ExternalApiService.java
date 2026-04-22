package com.divine.backendstage1.service;

import com.divine.backendstage1.exception.ExternalApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ExternalApiService {

    private final RestClient restClient = RestClient.create();

    private static final Map<Integer, String> ERROR_MESSAGES = Map.of(
            400, "API Rate limit or bad request!",
            429, "API Rate limit exceeded!"
    );

    @Async("externalApiExecutor")
    public CompletableFuture<Map<String, Object>> callGenderizeAsync(String name) {
        return CompletableFuture.completedFuture(callGenderize(name));
    }

    @Async("externalApiExecutor")
    public CompletableFuture<Map<String, Object>> callAgifyAsync(String name) {
        return CompletableFuture.completedFuture(callAgify(name));
    }

    @Async("externalApiExecutor")
    public CompletableFuture<Map<String, Object>> callNationalizeAsync(String name) {
        return CompletableFuture.completedFuture(callNationalize(name));
    }

    // --- Synchronous calls ---
    public Map<String, Object> callGenderize(String name) {
        return restClient.get()
                .uri("https://api.genderize.io/?name={name}", name)
                .retrieve()
                .onStatus(status -> status.value() >= 400, (request, response) -> {
                    throw new ExternalApiException(
                            ERROR_MESSAGES.getOrDefault(response.getStatusCode().value(),
                                    "Genderize server error"));
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> callAgify(String name) {
        return restClient.get()
                .uri("https://api.agify.io/?name={name}", name)
                .retrieve()
                .onStatus(status -> status.value() >= 400, (request, response) -> {
                    throw new ExternalApiException(
                            ERROR_MESSAGES.getOrDefault(response.getStatusCode().value(),
                                    "Agify server error"));
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> callNationalize(String name) {
        return restClient.get()
                .uri("https://api.nationalize.io/?name={name}", name)
                .retrieve()
                .onStatus(status -> status.value() >= 400, (request, response) -> {
                    throw new ExternalApiException(
                            ERROR_MESSAGES.getOrDefault(response.getStatusCode().value(),
                                    "Nationalize server error"));
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    // --- Batch parallel call ---
//    @Async("externalApiExecutor")
//    public CompletableFuture<List<Map<String, Object>>> callAllAsync(String name) {
//        List<CompletableFuture<Map<String, Object>>> futures = List.of(
//                callGenderizeAsync(name),
//                callAgifyAsync(name),
//                callNationalizeAsync(name)
//        );
//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> futures.stream()
//                        .map(CompletableFuture::join)
//                        .toList());
//    }

    // Remove @Async from callAllAsync entirely and run the work directly
    public CompletableFuture<List<Map<String, Object>>> callAllAsync(String name) {
        CompletableFuture<Map<String, Object>> genderFuture = callGenderizeAsync(name);
        CompletableFuture<Map<String, Object>> agifyFuture   = callAgifyAsync(name);
        CompletableFuture<Map<String, Object>> natFuture     = callNationalizeAsync(name);

        return CompletableFuture.allOf(genderFuture, agifyFuture, natFuture)
                .thenApply(v -> List.of(
                        genderFuture.join(),
                        agifyFuture.join(),
                        natFuture.join()
                ));
    }
}
