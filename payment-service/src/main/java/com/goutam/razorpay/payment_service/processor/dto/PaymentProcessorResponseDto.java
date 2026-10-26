package com.goutam.razorpay.payment_service.processor.dto;

public sealed interface PaymentProcessorResponseDto permits
        PaymentProcessorResponseDto.Success,
        PaymentProcessorResponseDto.Pending,
        PaymentProcessorResponseDto.Failure {

    record Success(String processorReference, String bankReference) implements PaymentProcessorResponseDto {}

    record Pending(String processorReference) implements PaymentProcessorResponseDto {}

    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponseDto {}
}
