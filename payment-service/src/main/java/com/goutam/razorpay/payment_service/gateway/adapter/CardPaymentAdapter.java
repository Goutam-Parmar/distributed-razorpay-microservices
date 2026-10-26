package com.goutam.razorpay.payment_service.gateway.adapter;


import com.goutam.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.goutam.razorpay.common_lib.dto.VaultChargeRequest;
import com.goutam.razorpay.payment_service.client.VaultServiceClient;
import com.goutam.razorpay.payment_service.gateway.PaymentAdapter;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentResultDto;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
@RequiredArgsConstructor
@Component
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    public PaymentResultDto initiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultServiceClient.charge(
                new VaultChargeRequest(request.paymentId(), token, request.amount(), request.methodDetails())
        );

        return switch (response) {
            case PaymentProcessorResponse.Success success -> new PaymentResultDto.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResultDto.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResultDto.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResultDto capture(UUID paymentId) {
        return new PaymentResultDto.Success("CARD_REF");
    }
}
