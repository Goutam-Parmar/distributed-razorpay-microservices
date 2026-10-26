package com.goutam.razorpay.merchant_service.mapper;

import com.goutam.razorpay.merchant_service.dto.request.MerchantSignupRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.MerchantResponseDto;
import com.goutam.razorpay.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {
    Merchant toEntityFromSignupRequestDto(MerchantSignupRequestDto request);

    MerchantResponseDto toResponseDto(Merchant merchant);
}
