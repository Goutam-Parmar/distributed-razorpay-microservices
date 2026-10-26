package com.goutam.razorpay.merchant_service.service.Implementation;


import com.goutam.razorpay.common_lib.dto.SettlementBankDetailsDto;
import com.goutam.razorpay.common_lib.dto.WebhookTargetDto;
import com.goutam.razorpay.common_lib.enums.MerchantStatus;
import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.merchant_service.api.MerchantLookupService;
import com.goutam.razorpay.merchant_service.entity.Merchant;
import com.goutam.razorpay.merchant_service.repository.MerchantRepository;
import com.goutam.razorpay.merchant_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantLookupServiceImpl implements MerchantLookupService {


    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository merchantWebhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;


    @Override
    public List<WebhookTargetDto> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return merchantWebhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(config -> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] cipherBytes = Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedSecretBytes = bytesEncryptor.decrypt(cipherBytes);
                    return new WebhookTargetDto(config.getId(), config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                })
                .toList();
    }

    @Override
    public List<UUID> listActiveMerchantIds() {
        return merchantRepository.findByStatus(MerchantStatus.ACTIVE)
                .stream().map(m -> m.getId()).toList();
    }

    @Override
    public SettlementBankDetailsDto getSettlementBankDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new ResourceNotFoundException("Merchant", merchantId));

        return new SettlementBankDetailsDto(
                merchant.getSettlementBankAccount(),
                merchant.getSettlementBankIfsc(),
                merchant.getSettlementBankAccountHolderName()
        );
    }
}
