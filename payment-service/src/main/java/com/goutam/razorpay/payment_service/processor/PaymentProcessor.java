package com.goutam.razorpay.payment_service.processor;

import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorRequestDto;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorResponseDto;

public interface PaymentProcessor {

    PaymentProcessorResponseDto charge(PaymentProcessorRequestDto request);
}
