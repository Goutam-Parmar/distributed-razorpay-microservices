package com.goutam.razorpay.api_gateway_service.security;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthHandler {

    private final JwtVerifier jwtVerifier;

    public Map<String, String> authenticate(String token) {
        Claims claims;
        try {
            claims = jwtVerifier.verify(token);
        }catch (Exception e) {
                log.error("JWT verification failed: {}", e.getMessage(), e);
                throw new GatewayAuthenticationException("Invalid or expired token");
            }
        return Map.of(
                "X-Merchant-Id", jwtVerifier.extractMerchantId(claims),
                "X-User-Role", jwtVerifier.extractRole(claims)
        );
    }
}

