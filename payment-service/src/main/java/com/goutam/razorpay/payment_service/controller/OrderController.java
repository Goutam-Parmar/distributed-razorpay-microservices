package com.goutam.razorpay.payment_service.controller;

import com.goutam.razorpay.common_lib.context.MerchantContext;
import com.goutam.razorpay.payment_service.dto.RequestDto.OrderRequestDto;
import com.goutam.razorpay.payment_service.dto.ResponseDto.OrderResponseDto;
import com.goutam.razorpay.payment_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final MerchantContext merchantContext;
    private final OrderService orderService;



    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto request) {
            return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderService.create(merchantContext.getMerchantId(), request));
    }
}
