// Carried over unchanged from the monolith's com.ecommerce.validation package,
// where these constraints existed but were never wired to a single endpoint.
// auth-service is the first place they do real work: the registration payload.
package com.ecommerce.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private boolean allowEmpty;
    private boolean checkDomain;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.checkDomain = constraintAnnotation.checkDomain();
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return allowEmpty;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        
        if (checkDomain) {
            return isValidDomain(email);
        }
        
        return true;
    }
    
    private boolean isValidDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        // Add domain validation logic here if needed
        return !domain.contains("..") && domain.length() > 0;
    }
}