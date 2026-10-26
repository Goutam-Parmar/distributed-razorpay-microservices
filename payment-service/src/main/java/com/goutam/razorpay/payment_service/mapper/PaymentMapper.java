package com.goutam.razorpay.payment_service.mapper;

import com.goutam.razorpay.payment_service.dto.ResponseDto.PaymentResponseDto;
import com.goutam.razorpay.payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {


    @Mapping(target = "orderId", source = "order.id")
    PaymentResponseDto toResponse(Payment payment);

    @Mapping(target = "orderId", source = "order.id")
    List<PaymentResponseDto> toResponseList(List<Payment> paymentList);
}
