package com.goutam.razorpay.vault_service.dto.request;

import com.goutam.razorpay.vault_service.Validator.ExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest(

        @NotNull(message = "PAN is Required")
        @LuhnCheck(message = "Invalid PAN")
        @Pattern(regexp = "^[0-9]{12,19}$", message = "PAN must be between 12 to 19 digits")
        String pan,

        @NotNull(message = "CVV is Required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
        String cvv,


        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry must be between 1 to 12")
        @Max(value = 12, message = "Expiry must be between 1 to 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @ExpiryYear
        Integer expiryYear,


        UUID customerId,

        @Size(min=3, message = "Card Holder Name must be at least 3 characters")
        String cardHolderName
) {
}
