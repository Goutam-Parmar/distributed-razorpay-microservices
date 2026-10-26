package com.goutam.razorpay.payment_service.service;


import com.goutam.razorpay.payment_service.dto.RequestDto.PaymentInitRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;

import java.util.UUID;

public interface PaymentService {

    PaymentResponseDto initiate(UUID merchantId, PaymentInitRequestDto request);

    PaymentResponseDto capture(UUID merchantId, UUID paymentId);
    void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription);
}
