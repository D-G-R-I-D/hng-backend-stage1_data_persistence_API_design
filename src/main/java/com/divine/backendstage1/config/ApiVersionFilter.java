package com.divine.backendstage1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class ApiVersionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public ApiVersionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply to /api/* endpoints
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiVersion = request.getHeader("X-API-Version");

        if (apiVersion == null || !apiVersion.equals("1")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "status", "error",
                    "message", "API version header required"
            )));
            return;
        }

        filterChain.doFilter(request, response);
    }
}