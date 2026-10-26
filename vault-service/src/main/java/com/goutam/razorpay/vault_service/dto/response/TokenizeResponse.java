package com.goutam.razorpay.vault_service.dto.response;


import com.goutam.razorpay.common_lib.enums.CardBand;

public record TokenizeResponse(
        String token ,
        String lastFour,
        CardBand brand,
        Integer expiryMonth,
        Integer expiryYear


) {
}
