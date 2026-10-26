package com.goutam.razorpay.payment_service.gateway.dto;


import com.goutam.razorpay.common_lib.entity.Money;
import com.goutam.razorpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(

        UUID merchantId,
        UUID orderId,
        UUID paymentId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails

) {
}
