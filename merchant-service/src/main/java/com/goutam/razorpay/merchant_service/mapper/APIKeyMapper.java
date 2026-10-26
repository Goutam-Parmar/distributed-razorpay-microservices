package com.goutam.razorpay.merchant_service.mapper;

import com.goutam.razorpay.merchant_service.dto.response.ApiKeyCreateResponseDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyResponseDto;
import com.goutam.razorpay.merchant_service.entity.APIKEY;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface APIKeyMapper {

    ApiKeyCreateResponseDto toCreateResponseDto(APIKEY apiKey);

    List<ApiKeyResponseDto> toResponseList(List<APIKEY> apiKeyList);
}
