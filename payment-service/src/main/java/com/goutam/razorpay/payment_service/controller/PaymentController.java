package com.goutam.razorpay.payment_service.controller;

import com.goutam.razorpay.common_lib.context.MerchantContext;
import com.goutam.razorpay.payment_service.dto.RequestDto.PaymentInitRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;
import com.goutam.razorpay.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> initiate(@Valid @RequestBody PaymentInitRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(merchantContext.getMerchantId(), request));
    }
    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponseDto> capture(@PathVariable UUID paymentId) {

        return ResponseEntity.ok(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}
