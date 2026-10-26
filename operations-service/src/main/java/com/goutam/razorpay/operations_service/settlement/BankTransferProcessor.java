package com.goutam.razorpay.operations_service.settlement;


import com.goutam.razorpay.common_lib.entity.Money;
import com.goutam.razorpay.operations_service.settlement.dto.BankTransferResultDto;

import java.util.UUID;

public interface BankTransferProcessor {

    BankTransferResultDto initiate(UUID settlementId, UUID merchantId, Money amount,
                                   String bankAccount, String ifsc);
}

