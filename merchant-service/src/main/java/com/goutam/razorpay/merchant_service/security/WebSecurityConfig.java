package com.goutam.razorpay.merchant_service.security;

import com.goutam.razorpay.common_lib.Idempotancy.IdempotencyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import org.springframework.core.Ordered;
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final String[] JWT_ROUTES = {
            "/v1/auth/**",
            "/v1/merchants/**",
            "/v1/admin/**",
            "/actuator/**",
            "/webhook/**"
    };

    private static final String[] API_KEY_ROUTES = {
            "/v1/orders/**",
            "/v1/payments/**",
            "/v1/vault/**"
    };

    private final IdempotencyFilter idempotencyFilter;

    @Bean
    public FilterRegistrationBean<Filter> idempotencyFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(idempotencyFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+2);
        registration.addUrlPatterns("/*");
        return registration;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}