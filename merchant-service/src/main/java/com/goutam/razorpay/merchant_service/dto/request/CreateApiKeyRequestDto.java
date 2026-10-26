package com.goutam.razorpay.merchant_service.dto.request;


import com.goutam.razorpay.common_lib.enums.Environment;

public record CreateApiKeyRequestDto(
        Environment environment
) {

}
