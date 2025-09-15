package com.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    
    String message() default "Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    int minLength() default 8;
    
    int maxLength() default 128;
    
    boolean requireUppercase() default true;
    
    boolean requireLowercase() default true;
    
    boolean requireDigit() default true;
    
    boolean requireSpecialChar() default true;
}