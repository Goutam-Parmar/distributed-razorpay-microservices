package com.goutam.razorpay.merchant_service.controller;


import com.goutam.razorpay.common_lib.context.MerchantContext;
import com.goutam.razorpay.merchant_service.dto.request.CreateApiKeyRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyCreateResponseDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyResponseDto;
import com.goutam.razorpay.merchant_service.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchant/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponseDto> create(
                                                          @Valid @RequestBody CreateApiKeyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponseDto>> listByMerchant() {
        return ResponseEntity.ok(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke( @PathVariable UUID keyId) {
        apiKeyService.revoke(merchantContext.getMerchantId(), keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponseDto> rotateKey( @PathVariable UUID keyId) {
        return ResponseEntity.ok(apiKeyService.rotate(merchantContext.getMerchantId(), keyId));
    }

}
