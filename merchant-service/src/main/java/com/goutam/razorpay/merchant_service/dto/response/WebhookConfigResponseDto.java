package com.goutam.razorpay.merchant_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookConfigResponseDto(
        UUID id,
        String targetUrl,
        String webhookSecret,
        boolean enabled,
        String eventTypes
) {
}
