package com.goutam.razorpay.payment_service.processor.dto;


import com.goutam.razorpay.common_lib.entity.Money;
import com.goutam.razorpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequestDto (
        UUID paymentId,
        UUID processingId,
        PaymentMethod method,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
){

    public static PaymentProcessorRequestDto card (UUID paymentId, String pan , String expiry, Money amount, Map<String, Object> details){
        return new PaymentProcessorRequestDto(UUID.randomUUID(), paymentId
                ,PaymentMethod.CARD, amount,
                pan, expiry, details);

    }

    public static PaymentProcessorRequestDto nonCard (UUID paymentId , PaymentMethod method ,  Money amount, Map<String, Object> details){
        return new PaymentProcessorRequestDto(UUID.randomUUID(), paymentId
                ,method, amount,null,null, details);

    }
}
