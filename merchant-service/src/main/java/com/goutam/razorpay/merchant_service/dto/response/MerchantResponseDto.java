package com.goutam.razorpay.merchant_service.dto.response;


import com.goutam.razorpay.common_lib.enums.BusinessType;
import com.goutam.razorpay.common_lib.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponseDto(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
