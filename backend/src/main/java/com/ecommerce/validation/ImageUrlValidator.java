package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {
    
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );
    
    @Override
    public void initialize(ValidImageUrl constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String imageUrl, ConstraintValidatorContext context) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true; // Optional field
        }
        
        try {
            // Validate URL format
            URL url = new URL(imageUrl);
            
            // Check if URL uses HTTP or HTTPS
            String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Image URL must use HTTP or HTTPS protocol")
                        .addConstraintViolation();
                return false;
            }
            
            // Check file extension
            String path = url.getPath();
            if (path != null && !path.isEmpty()) {
                String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
                if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            "Image URL must point to a valid image file (jpg, jpeg, png, gif, bmp, webp, svg)"
                    ).addConstraintViolation();
                    return false;
                }
            }
            
            return true;
            
        } catch (MalformedURLException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid URL format")
                    .addConstraintViolation();
            return false;
        }
    }
}
