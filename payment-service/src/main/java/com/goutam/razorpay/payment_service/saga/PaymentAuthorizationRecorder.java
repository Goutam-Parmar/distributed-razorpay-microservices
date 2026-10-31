package com.goutam.razorpay.payment_service.saga;


import com.goutam.razorpay.common_lib.enums.EventAggregateType;
import com.goutam.razorpay.common_lib.enums.OrderStatus;
import com.goutam.razorpay.common_lib.enums.PaymentEvent;
import com.goutam.razorpay.common_lib.enums.PaymentStatus;
import com.goutam.razorpay.common_lib.exception.BusinessRuleViolationException;
import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.payment_service.dto.RequestDto.PaymentInitRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;
import com.goutam.razorpay.payment_service.entity.OrderRecord;
import com.goutam.razorpay.payment_service.entity.Payment;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentResultDto;
import com.goutam.razorpay.payment_service.mapper.PaymentMapper;
import com.goutam.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.goutam.razorpay.payment_service.repository.OrderRepository;
import com.goutam.razorpay.payment_service.repository.PaymentRepository;
import com.goutam.razorpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAuthorizationRecorder {

    public final OrderRepository orderRepository;
    public  final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitRequestDto request ){
        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED) {
            throw new BusinessRuleViolationException("Order_Not_Payable","Order Can not Payable in status :"+order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment= Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .idempotencyKey(UUID.randomUUID().toString())
                .methodDetails(request.methodDetails())
                .build();

        payment=  paymentRepository.save(payment);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    @Transactional
    public PaymentResponseDto compensateAuthorizationFailure(UUID paymentId, String errorCode,
                                                          String errorDescription) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment = paymentRepository.save(payment);

        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toResponse(payment);
    }
    @Transactional
    public PaymentResponseDto applyGatewayResult(UUID paymentId, PaymentResultDto result) {
        log.info("Applying Gateway result for paymentId: {}", paymentId);
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        switch (result) {
            case PaymentResultDto.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResultDto.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResultDto.Success success ->
                    log.warn("Invalid state: initiate() gateway call returned Success directly, paymentId={}", paymentId);
        }

        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_CREATED");
        log.info("Successfully applied Gateway result for paymentId: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }
    private void publishStatusEvent(Payment payment, String eventType) {
        eventPublisher.publish(  EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                ));
    }
}
