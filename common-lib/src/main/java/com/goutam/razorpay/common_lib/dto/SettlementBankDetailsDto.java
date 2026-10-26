package com.goutam.razorpay.common_lib.dto;

public record SettlementBankDetailsDto(
        String accountNumber, String ifsc, String accountHolderName
) {
}
