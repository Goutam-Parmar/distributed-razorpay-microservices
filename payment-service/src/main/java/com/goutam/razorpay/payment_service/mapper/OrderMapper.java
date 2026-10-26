package com.goutam.razorpay.payment_service.mapper;

import com.goutam.razorpay.payment_service.dto.ResponseDto.OrderResponseDto;
import com.goutam.razorpay.payment_service.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(target = "status", source = "orderStatus")
    OrderResponseDto toResponse(OrderRecord orderRecord);
}
