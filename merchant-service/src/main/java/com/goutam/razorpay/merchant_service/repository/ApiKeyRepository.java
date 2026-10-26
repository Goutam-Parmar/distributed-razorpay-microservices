package com.goutam.razorpay.merchant_service.repository;


import com.goutam.razorpay.merchant_service.entity.APIKEY;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<APIKEY, UUID> {
    List<APIKEY> findByMerchant_Id(UUID merchantId);

   Optional<APIKEY> findByKeyId(String keyId);
}

