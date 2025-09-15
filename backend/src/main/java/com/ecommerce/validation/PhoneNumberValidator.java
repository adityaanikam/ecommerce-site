package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    private boolean allowEmpty;
    private String countryCode;
    
    private static final Pattern US_PHONE_PATTERN = Pattern.compile(
        "^\\+?1?[-.\\s]?\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$"
    );
    
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
        "^\\+[1-9]\\d{1,14}$"
    );
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.countryCode = constraintAnnotation.countryCode();
    }
    
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return allowEmpty;
        }
        
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");
        
        if ("US".equals(countryCode)) {
            return US_PHONE_PATTERN.matcher(phoneNumber).matches();
        } else {
            return INTERNATIONAL_PHONE_PATTERN.matcher(cleaned).matches();
        }
    }
}