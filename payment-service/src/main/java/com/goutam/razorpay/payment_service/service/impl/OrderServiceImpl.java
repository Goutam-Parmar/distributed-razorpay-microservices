package com.goutam.razorpay.payment_service.service.impl;

import com.goutam.razorpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.goutam.razorpay.common_lib.enums.EventAggregateType;
import com.goutam.razorpay.common_lib.enums.OrderStatus;
import com.goutam.razorpay.common_lib.exception.BusinessRuleViolationException;
import com.goutam.razorpay.common_lib.exception.DuplicateResourceException;
import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.payment_service.client.CustomerServiceClient;
import com.goutam.razorpay.payment_service.dto.RequestDto.OrderRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.OrderResponseDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;
import com.goutam.razorpay.payment_service.entity.OrderRecord;
import com.goutam.razorpay.payment_service.entity.Payment;
import com.goutam.razorpay.payment_service.mapper.OrderMapper;
import com.goutam.razorpay.payment_service.mapper.PaymentMapper;
import com.goutam.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.goutam.razorpay.payment_service.repository.OrderRepository;
import com.goutam.razorpay.payment_service.repository.PaymentRepository;
import com.goutam.razorpay.payment_service.service.OrderPersistenceService;
import com.goutam.razorpay.payment_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private  final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerServiceClient  customerServiceClient;
    private final OutboxEventPublisher eventPublisher;
    private final OrderPersistenceService orderPersistenceService;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
   public OrderResponseDto create(UUID merchantId, OrderRequestDto request) {
        if(request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE","Order with this receipt already exists for the merchant "+request.receipt());
        }
        UUID customerId = null;
        if (request.customer() != null) {
            customerId = customerServiceClient.findOrCreate(
                    new FindOrCreateCustomerRequest(merchantId,
                            request.customer().email(),
                            request.customer().name(),
                            request.customer().phone())
            );
        }


        return orderPersistenceService.persist(merchantId, request, customerId, defaultOrderExpiryMinutes);


    }

    @Override
    public OrderResponseDto getById(UUID merchantId, UUID orderId) {
        OrderRecord order =  orderRepository.findByIdAndMerchantId(orderId ,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found ", orderId));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto cancel(UUID merchantId, UUID orderId) {
        OrderRecord order =  orderRepository.findByIdAndMerchantId(orderId ,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found ", orderId));

        if(order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID){
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL","can not cancel order with status "+order.getOrderStatus().name());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CANCELLED",
                Map.of("orderId", order.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );
        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponseDto> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order =  orderRepository.findByIdAndMerchantId(orderId ,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found ", orderId));

        List<Payment> paymentList = paymentRepository.findByOrder_Id(orderId);



        return paymentMapper.toResponseList(paymentList);
    }
}
