package com.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    
    String message() default "Invalid email format";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    boolean allowEmpty() default false;
    
    boolean checkDomain() default false;
}