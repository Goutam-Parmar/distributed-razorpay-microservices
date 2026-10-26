package com.goutam.razorpay.merchant_service.dto.response;


import com.goutam.razorpay.common_lib.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponseDto(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment
) {
}
