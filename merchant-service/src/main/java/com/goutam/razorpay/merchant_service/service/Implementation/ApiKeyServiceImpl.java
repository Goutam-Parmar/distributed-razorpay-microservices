package com.goutam.razorpay.merchant_service.service.Implementation;


import com.goutam.razorpay.common_lib.cache.ApiKeyCache;
import com.goutam.razorpay.common_lib.exception.ResourceNotFoundException;
import com.goutam.razorpay.common_lib.util.RandomizerUtil;
import com.goutam.razorpay.merchant_service.dto.request.CreateApiKeyRequestDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyCreateResponseDto;
import com.goutam.razorpay.merchant_service.dto.response.ApiKeyResponseDto;
import com.goutam.razorpay.merchant_service.entity.APIKEY;
import com.goutam.razorpay.merchant_service.entity.Merchant;
import com.goutam.razorpay.merchant_service.mapper.APIKeyMapper;
import com.goutam.razorpay.merchant_service.repository.ApiKeyRepository;
import com.goutam.razorpay.merchant_service.repository.MerchantRepository;
import com.goutam.razorpay.merchant_service.service.ApiKeyService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final APIKeyMapper apiKeyMapper;
    private  final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public ApiKeyCreateResponseDto create(UUID merchantId, CreateApiKeyRequestDto request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);

        APIKEY apiKey = APIKEY.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret))
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponseDto(apiKey.getId(), keyId, rawSecret, request.environment());
    }


    @Override
    public List<ApiKeyResponseDto> listByMerchant(UUID merchantId) {
        return apiKeyMapper.toResponseList(apiKeyRepository.findByMerchant_Id(merchantId));
    }
    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        APIKEY key = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));

        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());
    }

    @Override
    @Transactional
    public @Nullable ApiKeyCreateResponseDto rotate(UUID merchantId, UUID keyId) {
        APIKEY apiKey = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));

        if(!apiKey.isEnabled()){
            throw new RuntimeException("Cannot rotate a disabled API key");
        }
        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        apiKey = apiKeyRepository.save(apiKey);


        apiKeyCache.evict(apiKey.getKeyId());
        return new ApiKeyCreateResponseDto(apiKey.getId(), apiKey.getKeyId(),
                newRawSecret, apiKey.getEnvironment());
    }

}
