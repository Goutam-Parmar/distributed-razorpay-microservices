package com.goutam.razorpay.payment_service.gateway.adapter;

import com.goutam.razorpay.common_lib.enums.PaymentMethod;
import com.goutam.razorpay.payment_service.gateway.PaymentAdapter;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentResultDto;
import com.goutam.razorpay.payment_service.processor.PaymentProcessorRouter;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorRequestDto;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {


    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResultDto initiate(PaymentRequest request) {
        log.info("Initiating Net Banking payment Adapter , paymentId: {}", request.paymentId());
        try {


            PaymentProcessorRequestDto paymentProcessorRequest = PaymentProcessorRequestDto.nonCard(
                    request.paymentId(),
                    PaymentMethod.NETBANKING,
                    request.amount(),
                    request.methodDetails()
            );

            PaymentProcessorResponseDto paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);


            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponseDto.Failure failure ->
                        new PaymentResultDto.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponseDto.Pending pending ->
                        new PaymentResultDto.Pending(pending.processorReference());
                case PaymentProcessorResponseDto.Success success ->
                        new PaymentResultDto.Success(success.bankReference());
            };
        }catch (Exception e){
            log.warn("NetBanking failed , paymentId:{}", request.paymentId());
            return new PaymentResultDto.Failure("NETBANKING_ADAPTER_Failed", e.getMessage());
        }
    }

    @Override
    public PaymentResultDto capture(UUID paymentId) {
        return new PaymentResultDto.Success("NetBanking Reference");
    }
}
