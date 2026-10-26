package com.goutam.razorpay.operations_service.client;

import com.goutam.razorpay.common_lib.dto.SettlementBankDetailsDto;
import com.goutam.razorpay.common_lib.dto.WebhookTargetDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "merchant-service", path = "/internal/merchants", url = "${MERCHANT_SERVICE_URI}")
public interface MerchantServiceClient {

    @GetMapping("/{merchantId}/webhook-targets")
    List<WebhookTargetDto> getActiveConfigsForEvent(@PathVariable UUID merchantId, @RequestParam String eventType);

    @GetMapping("/active-ids")
    List<UUID> listActiveMerchantIds();

    @GetMapping("/{merchantId}/settlement-bank-details")
    SettlementBankDetailsDto getSettlementBankDetails(@PathVariable UUID merchantId);
}
