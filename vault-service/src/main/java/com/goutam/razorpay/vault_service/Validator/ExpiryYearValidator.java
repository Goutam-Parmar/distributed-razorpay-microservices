package com.goutam.razorpay.vault_service.Validator;

import jakarta.validation.ConstraintValidator;

public class ExpiryYearValidator implements ConstraintValidator<ExpiryYear, Integer> {

    @Override
    public boolean isValid(Integer value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null values are considered valid
        }
        int currentYear = java.time.Year.now().getValue();
        return value >= currentYear;
    }
}
