package com.divine.backendstage1.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter(
            RequestLoggingFilter filter) {
        FilterRegistrationBean<RequestLoggingFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ApiVersionFilter> apiVersionFilter(
            ApiVersionFilter filter) {
        FilterRegistrationBean<ApiVersionFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(3);
        return bean;
    }
}