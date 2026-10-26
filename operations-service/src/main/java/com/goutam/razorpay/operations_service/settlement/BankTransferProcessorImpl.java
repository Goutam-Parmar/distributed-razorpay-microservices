package com.goutam.razorpay.operations_service.settlement;

import com.goutam.razorpay.common_lib.entity.Money;
import com.goutam.razorpay.common_lib.util.RandomizerUtil;
import com.goutam.razorpay.operations_service.settlement.dto.BankTransferResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BankTransferProcessorImpl implements BankTransferProcessor {

    @Override
    public BankTransferResultDto initiate(UUID settlementId, UUID merchantId, Money amount,
                                          String bankAccount, String ifsc) {

        // Call the Bank API

        String registrationRef = "TXN_"+ RandomizerUtil.randomBase64(12);

        log.debug("Bank Transfer call completed for settlementId: {}, registrationRef: {}",
                settlementId, registrationRef);

        return new BankTransferResultDto(registrationRef);
    }
}
