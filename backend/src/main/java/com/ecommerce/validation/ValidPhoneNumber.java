package com.ecommerce.validation;

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