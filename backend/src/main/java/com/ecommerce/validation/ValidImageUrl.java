package com.ecommerce.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageUrlValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageUrl {
    String message() default "Image URL should be valid and point to an image file";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
