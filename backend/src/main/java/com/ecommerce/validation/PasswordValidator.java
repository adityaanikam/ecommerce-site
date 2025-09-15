package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Check length
        if (password.length() < minLength || password.length() > maxLength) {
            return false;
        }
        
        // Check uppercase requirement
        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }
        
        // Check lowercase requirement
        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }
        
        // Check digit requirement
        if (requireDigit && !DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }
        
        // Check special character requirement
        if (requireSpecialChar && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return false;
        }
        
        return true;
    }
}