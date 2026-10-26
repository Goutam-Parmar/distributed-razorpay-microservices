package com.goutam.razorpay.merchant_service.service;


import com.goutam.razorpay.merchant_service.dto.request.LoginRequest;
import com.goutam.razorpay.merchant_service.dto.request.MerchantSignupRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.LoginResponse;
import com.goutam.razorpay.merchant_service.dto.response.MerchantResponseDto;

public interface AuthService {
    MerchantResponseDto signup(MerchantSignupRequestDto request);

    LoginResponse login(LoginRequest request);
}
