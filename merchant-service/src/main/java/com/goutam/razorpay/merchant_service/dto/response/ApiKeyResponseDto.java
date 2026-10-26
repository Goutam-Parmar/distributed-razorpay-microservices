package com.goutam.razorpay.merchant_service.dto.response;


import com.goutam.razorpay.common_lib.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponseDto(
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {
}
