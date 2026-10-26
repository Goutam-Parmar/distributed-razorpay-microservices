package com.goutam.razorpay.payment_service.client;

import com.goutam.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.goutam.razorpay.common_lib.dto.VaultChargeRequest;
import com.goutam.razorpay.payment_service.processor.dto.PaymentProcessorResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", path = "/internal/vault", url = "${VAULT_SERVICE_URI:}")
public interface VaultServiceClient {

    @PostMapping("/charge")
    PaymentProcessorResponse charge(@RequestBody VaultChargeRequest request);
}
