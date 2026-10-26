package com.goutam.razorpay.payment_service.processor;

import com.goutam.razorpay.common_lib.enums.PaymentMethod;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorRequestDto;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessorResponseDto charge(PaymentProcessorRequestDto request) {

        PaymentProcessor processor = paymentProcessors.get(request.method());

        if (processor == null) {
            throw new IllegalArgumentException(
                    "No payment processor found for method: " + request.method());
        }

        return processor.charge(request);
    }
}