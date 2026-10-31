package com.goutam.razorpay.payment_service.service.impl;

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
import com.goutam.razorpay.payment_service.gateway.PaymentGatewayRouter;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentResultDto;
import com.goutam.razorpay.payment_service.mapper.PaymentMapper;
import com.goutam.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.goutam.razorpay.payment_service.repository.OrderRepository;
import com.goutam.razorpay.payment_service.repository.PaymentRepository;
import com.goutam.razorpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.goutam.razorpay.payment_service.service.PaymentService;
import com.goutam.razorpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
@Service
@Slf4j
@RequiredArgsConstructor

public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Transactional
    @Override
    public PaymentResponseDto initiate(UUID merchantId, PaymentInitRequestDto request) {
       Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId, request);


        PaymentRequest paymentRequest = new PaymentRequest(
                merchantId,
                request.orderId(),
                payment.getId(),
                payment.getAmount(),
                request.method(),
                request.methodDetails()
        );


        PaymentResultDto result;
        try {
            result = paymentGatewayRouter.initiate(paymentRequest);
        } catch (Exception e) {
            return paymentAuthorizationRecorder.compensateAuthorizationFailure(payment.getId(),
                    "PAYMENT_GATEWAY_ROUTER_UNREACHABLE", e.getMessage());
        }

        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(), result);
    }

    @Override
    @Transactional
    public PaymentResponseDto capture(UUID merchantId, UUID paymentId) {

//        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
//

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));



        paymentTransitionService .apply(payment, PaymentEvent.CAPTURE_REQUEST);
        PaymentResultDto paymentResult = paymentGatewayRouter.capture(payment.getMethod(),paymentId);

        if(paymentResult instanceof PaymentResultDto.Success success) {

            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment Captured successfully for paymentId: {}, merchantId: {}", paymentId, merchantId);
        } else if (paymentResult instanceof PaymentResultDto.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
    log.warn("Payment Capture failed for paymentId: {}",paymentId);
        }

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchantId.toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                )
        );

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }





    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve,
                                     String bankRef, String errorCode, String errorDescription) {

//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getStatus() != PaymentStatus.AUTHORIZING) {
            log.warn("Payment is not in Authorizing state, paymentID: {}, status: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();

        if (approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            // Auto-capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResultDto captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if(captureResult instanceof PaymentResultDto.Success success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            } else if (captureResult instanceof  PaymentResultDto.Failure failure){
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                )
        );
    }
}
