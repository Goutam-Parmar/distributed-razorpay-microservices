package com.goutam.razorpay.payment_service.gateway.dto;

public sealed interface PaymentResultDto permits
        PaymentResultDto.Success,
        PaymentResultDto.Pending,
        PaymentResultDto.Failure {

    record Pending(String registrationRef) implements PaymentResultDto {}

    record Failure(String errorCode, String errorDescription) implements PaymentResultDto {}

    record Success(String bankReference) implements PaymentResultDto {}
}
