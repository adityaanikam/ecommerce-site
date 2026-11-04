// Carried over unchanged from the monolith's com.ecommerce.validation package,
// where these constraints existed but were never wired to a single endpoint.
// auth-service is the first place they do real work: the registration payload.
package com.ecommerce.auth.validation;

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