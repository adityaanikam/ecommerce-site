// Carried over unchanged from the monolith's com.ecommerce.validation package,
// where these constraints existed but were never wired to a single endpoint.
// auth-service is the first place they do real work: the registration payload.
package com.ecommerce.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    
    String message() default "Invalid phone number format";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    boolean allowEmpty() default true;
    
    String countryCode() default "US";
}