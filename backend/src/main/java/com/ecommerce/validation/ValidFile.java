package com.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {
    
    String message() default "Invalid file";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    long maxSize() default 10485760; // 10MB default
    
    String[] allowedTypes() default {"image/jpeg", "image/png", "image/gif", "image/webp"};
    
    boolean allowEmpty() default false;
}
