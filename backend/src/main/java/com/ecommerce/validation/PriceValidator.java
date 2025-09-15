package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceValidator implements ConstraintValidator<ValidPrice, Number> {
    
    private double min;
    private double max;
    private int decimalPlaces;
    
    @Override
    public void initialize(ValidPrice constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.decimalPlaces = constraintAnnotation.decimalPlaces();
    }
    
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null values
        }
        
        double doubleValue = value.doubleValue();
        
        // Check range
        if (doubleValue < min || doubleValue > max) {
            return false;
        }
        
        // Check decimal places
        if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            return decimal.scale() <= decimalPlaces;
        }
        
        // For other number types, check if the value has too many decimal places
        BigDecimal decimal = BigDecimal.valueOf(doubleValue);
        decimal = decimal.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return decimal.doubleValue() == doubleValue;
    }
}