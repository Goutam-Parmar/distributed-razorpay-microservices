package com.goutam.razorpay.payment_service.repository;

import com.goutam.razorpay.payment_service.entity.OrderRecord;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {

    Optional<OrderRecord> findByIdAndMerchantId(
            UUID orderId,
            UUID merchantId
    );

    boolean existsByMerchantIdAndReceipt(
            UUID merchantId,
            @Size(max = 100) String receipt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM OrderRecord o
            WHERE o.id = :orderId
              AND o.merchantId = :merchantId
            """)
    Optional<OrderRecord> findByIdAndMerchantIdForUpdate(
            @Param("orderId") UUID orderId,
            @Param("merchantId") UUID merchantId
    );
}
