package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    
    private long maxSize;
    private String[] allowedTypes;
    private boolean allowEmpty;
    
    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }
    
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return allowEmpty;
        }
        
        // Check file size
        if (file.getSize() > maxSize) {
            return false;
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes).contains(contentType)) {
            return false;
        }
        
        return true;
    }
}
