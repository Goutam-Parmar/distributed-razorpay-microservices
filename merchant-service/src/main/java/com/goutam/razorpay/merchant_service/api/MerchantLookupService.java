package com.goutam.razorpay.merchant_service.api;


import com.goutam.razorpay.common_lib.dto.SettlementBankDetailsDto;
import com.goutam.razorpay.common_lib.dto.WebhookTargetDto;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {
    List<WebhookTargetDto> getActiveConfigsForEvent(UUID merchantId, String eventType);

    List<UUID> listActiveMerchantIds();

    SettlementBankDetailsDto getSettlementBankDetails(UUID merchantId);
}
