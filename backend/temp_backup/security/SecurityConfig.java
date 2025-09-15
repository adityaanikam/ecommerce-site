package com.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    private final CorsConfig corsConfig;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/reviews/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                
                // Health and monitoring endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                
                // Swagger/OpenAPI documentation
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // Static resources
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/orders/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/products", "POST").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/api/products/**", "PUT").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers("/api/products/**", "DELETE").hasRole("ADMIN")
                
                // User endpoints
                .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/orders").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
                
                // Seller endpoints
                .requestMatchers("/api/seller/**").hasAnyRole("SELLER", "ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 configuration
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google")
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(new CustomOAuth2UserService())
                )
            )
            
            // Authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add custom filters
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, OAuth2LoginAuthenticationFilter.class);
        
        return http.build();
    }
}