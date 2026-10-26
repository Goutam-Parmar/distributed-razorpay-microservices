package com.goutam.razorpay.payment_service.gateway;


import com.goutam.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.goutam.razorpay.payment_service.gateway.dto.PaymentResultDto;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResultDto initiate(PaymentRequest request);

    PaymentResultDto capture(UUID paymentId);
}
