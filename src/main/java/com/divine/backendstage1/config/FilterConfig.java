package com.divine.backendstage1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final ObjectMapper objectMapper;

    public FilterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> bean =
                new FilterRegistrationBean<>(new RequestLoggingFilter());
        bean.setOrder(-200);  // was 1
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> bean =
                new FilterRegistrationBean<>(new RateLimitFilter(objectMapper));
        bean.setOrder(-150);  // was 2
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ApiVersionFilter> apiVersionFilter() {
        FilterRegistrationBean<ApiVersionFilter> bean =
                new FilterRegistrationBean<>(new ApiVersionFilter(objectMapper));
        bean.setOrder(-101);  // was 3 — just before Spring Security's -100
        return bean;
    }
}

























//package com.divine.backendstage1.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FilterConfig {
//
//    private final ObjectMapper objectMapper;
//
//    public FilterConfig(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    @Bean
//    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter(
//            RequestLoggingFilter filter) {
//        FilterRegistrationBean<RequestLoggingFilter> bean = new FilterRegistrationBean<>(filter);
//        bean.setOrder(1);
//        return bean;
//    }
//
//    @Bean
//    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
//            RateLimitFilter filter) {
//        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>(filter);
//        bean.setOrder(2);
//        return bean;
//    }
//
//    @Bean
//    public FilterRegistrationBean<ApiVersionFilter> apiVersionFilter(
//            ApiVersionFilter filter) {
//        FilterRegistrationBean<ApiVersionFilter> bean = new FilterRegistrationBean<>(filter);
//        bean.setOrder(3);
//        return bean;
//    }
//}