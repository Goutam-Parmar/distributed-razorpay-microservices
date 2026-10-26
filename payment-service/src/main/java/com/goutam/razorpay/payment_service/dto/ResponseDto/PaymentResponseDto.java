package com.goutam.razorpay.payment_service.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.goutam.razorpay.common_lib.entity.Money;
import com.goutam.razorpay.common_lib.enums.PaymentMethod;
import com.goutam.razorpay.common_lib.enums.PaymentStatus;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponseDto(
        UUID id,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentStatus status,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt,
        LocalDateTime createdAt
) {
}
