package com.goutam.razorpay.payment_service.service;


import com.goutam.razorpay.payment_service.dto.RequestDto.OrderRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.OrderResponseDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDto create(UUID merchantId, OrderRequestDto request);

    OrderResponseDto getById(UUID merchantId, UUID orderId);

    OrderResponseDto cancel(UUID merchantId, UUID orderId);

    List<PaymentResponseDto> listPayments(UUID merchantId, UUID orderId);
}
