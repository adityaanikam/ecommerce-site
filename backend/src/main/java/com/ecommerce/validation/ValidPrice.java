package com.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PriceValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrice {
    
    String message() default "Invalid price value";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    double min() default 0.01;
    
    double max() default Double.MAX_VALUE;
    
    int decimalPlaces() default 2;
}