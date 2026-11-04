package com.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * BCrypt at strength 10, the Spring default. The cost is the point: it makes an
     * offline attack on a leaked password_hash column slow. Raising it is a
     * one-character change, and BCrypt stores the cost in the hash, so old hashes
     * keep verifying after a bump.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
