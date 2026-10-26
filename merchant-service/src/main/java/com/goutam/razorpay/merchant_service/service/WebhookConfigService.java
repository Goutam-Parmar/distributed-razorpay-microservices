package com.goutam.razorpay.merchant_service.service;

import com.goutam.razorpay.merchant_service.dto.request.UpdateWebhookConfigRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.WebhookConfigResponseDto;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {
    WebhookConfigResponseDto create(UUID merchantId, UpdateWebhookConfigRequestDto request);

    List<WebhookConfigResponseDto> list(UUID merchantId);

    WebhookConfigResponseDto getById(UUID merchantId, UUID configId);

    WebhookConfigResponseDto update(UUID merchantId, UUID configId, UpdateWebhookConfigRequestDto request);

    void delete(UUID merchantId, UUID configId);
}
