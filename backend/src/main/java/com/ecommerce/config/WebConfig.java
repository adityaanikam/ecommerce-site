package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:3001", "http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve product images from the Products folder with URL decoding
        registry.addResourceHandler("/products/**")
                .addResourceLocations("file:C:/Users/adity/IdeaProjects/ecommerece project/backend/Products/")
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // URL decode the resource path to handle + characters
                        String decodedPath = URLDecoder.decode(resourcePath, StandardCharsets.UTF_8);
                        return super.getResource(decodedPath, location);
                    }
                });
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // Set appropriate content type for image requests
                if (request.getRequestURI().startsWith("/products/")) {
                    String uri = request.getRequestURI();
                    String extension = uri.substring(uri.lastIndexOf('.') + 1).toLowerCase();
                    
                    switch (extension) {
                        case "jpg", "jpeg" -> response.setContentType(MediaType.IMAGE_JPEG_VALUE);
                        case "png" -> response.setContentType(MediaType.IMAGE_PNG_VALUE);
                        case "webp" -> response.setContentType("image/webp");
                        case "gif" -> response.setContentType(MediaType.IMAGE_GIF_VALUE);
                        default -> response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    }
                    
                    // Set cache headers for better performance
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
                    response.setHeader(HttpHeaders.EXPIRES, String.valueOf(System.currentTimeMillis() + 3600000));
                }
                
                return true;
            }
        });
    }
}
