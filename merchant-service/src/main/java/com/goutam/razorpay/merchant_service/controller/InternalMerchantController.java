package com.goutam.razorpay.merchant_service.controller;
import com.goutam.razorpay.common_lib.dto.SettlementBankDetailsDto;
import com.goutam.razorpay.common_lib.dto.WebhookTargetDto;
import com.goutam.razorpay.merchant_service.api.MerchantLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/merchants")
public class InternalMerchantController {

    private final MerchantLookupService merchantLookupService;

    @GetMapping("/{merchantId}/webhook-targets")
    public List<WebhookTargetDto> getActiveConfigsForEvent(@PathVariable UUID merchantId,
                                                           @RequestParam String eventType) {
        return merchantLookupService.getActiveConfigsForEvent(merchantId, eventType);
    }

    @GetMapping("/active-ids")
    public List<UUID> listActiveMerchantIds() {
        return merchantLookupService.listActiveMerchantIds();
    }

    @GetMapping("/{merchantId}/settlement-bank-details")
    public SettlementBankDetailsDto getSettlementBankDetails(@PathVariable UUID merchantId) {
        return merchantLookupService.getSettlementBankDetails(merchantId);
    }
}
