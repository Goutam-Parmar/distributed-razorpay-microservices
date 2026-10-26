package com.goutam.razorpay.merchant_service.service.Implementation;


import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.common_lib.util.RandomizerUtil;
import com.goutam.razorpay.merchant_service.dto.request.UpdateWebhookConfigRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.WebhookConfigResponseDto;
import com.goutam.razorpay.merchant_service.entity.Merchant;
import com.goutam.razorpay.merchant_service.entity.MerchantWebhookConfig;
import com.goutam.razorpay.merchant_service.mapper.WebhookConfigMapper;
import com.goutam.razorpay.merchant_service.repository.MerchantRepository;
import com.goutam.razorpay.merchant_service.repository.WebhookConfigRepository;
import com.goutam.razorpay.merchant_service.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService {


    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigRepository merchantWebhookConfigRepository;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponseDto create(UUID merchantId, UpdateWebhookConfigRequestDto request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);

        String encryptedSecret = Base64.getEncoder().encodeToString
                (bytesEncryptor.encrypt(rawSecretBytes));

        MerchantWebhookConfig config = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        config = merchantWebhookConfigRepository.save(config);

        return webhookConfigMapper.toResponse(config, rawSecret);
    }

    @Override
    public List<WebhookConfigResponseDto> list(UUID merchantId) {
        return merchantWebhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponseDto getById(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        return webhookConfigMapper.toResponse(config, null);
    }


    @Override
    @Transactional
    public WebhookConfigResponseDto update(UUID merchantId, UUID configId, UpdateWebhookConfigRequestDto request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());
        log.info("Merchant webhook config updated id={} merchantId={}", configId, merchantId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        merchantWebhookConfigRepository.delete(config);
        log.info("Merchant webhook config deleted id={} merchantId={}", configId, merchantId);
    }
    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId) {
        return merchantWebhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", configId));
    }

//    @Override
//    public List<WebhookTargetDto> getActiveConfigForEvent(UUID merchantId , String eventType){
//        merchantWebhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
//                .filter(config ->config.isSub)
//    }
}
