package com.goutam.razorpay.common_lib.dto;

import java.util.UUID;

public record WebhookTargetDto(UUID configId, String targetUrl , String webhookSecret) {
}
