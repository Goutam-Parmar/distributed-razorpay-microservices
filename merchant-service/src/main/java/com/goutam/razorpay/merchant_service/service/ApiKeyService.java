package com.goutam.razorpay.merchant_service.service;


import com.goutam.razorpay.merchant_service.dto.request.CreateApiKeyRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyCreateResponseDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyResponseDto;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;


public interface ApiKeyService {
    ApiKeyCreateResponseDto create(UUID merchantId, CreateApiKeyRequestDto request);

    List<ApiKeyResponseDto> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    @Nullable
    ApiKeyCreateResponseDto rotate(UUID merchantId, UUID keyId);
}
