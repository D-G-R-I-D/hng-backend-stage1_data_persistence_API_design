//package com.divine.backendstage1.config;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletResponse;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.filter.OncePerRequestFilter;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.IOException;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public OncePerRequestFilter corsFilter() {
//        return new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(@NotNull HttpServletRequest req,
//                                            @NotNull HttpServletResponse res,
//                                            @NotNull FilterChain chain)
//                    throws ServletException, IOException {
//                res.setHeader("Access-Control-Allow-Origin", "*");
//                res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//                res.setHeader("Access-Control-Allow-Headers", "*");
//                if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
//                    res.setStatus(HttpServletResponse.SC_OK);
//                    return;
//                }
//                chain.doFilter(req, res);
//            }
//        };
//    }
//}