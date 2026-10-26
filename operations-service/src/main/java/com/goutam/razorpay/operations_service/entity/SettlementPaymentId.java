package com.goutam.razorpay.operations_service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SettlementPaymentId  {


    private UUID settlementId;

    private UUID paymentId;
}
