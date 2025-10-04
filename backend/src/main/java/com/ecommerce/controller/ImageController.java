package com.ecommerce.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageController {

    private static final String PRODUCTS_BASE_PATH = "C:\\Users\\adity\\IdeaProjects\\ecommerece project\\backend\\Products\\";

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Image controller is working!");
    }

    @GetMapping("/product/{category}/{productName}/{imageName}")
    public ResponseEntity<Resource> getProductImage(
            @PathVariable String category,
            @PathVariable String productName,
            @PathVariable String imageName) {
        
        try {
            // URL decode the parameters
            String decodedCategory = URLDecoder.decode(category, StandardCharsets.UTF_8);
            String decodedProductName = URLDecoder.decode(productName, StandardCharsets.UTF_8);
            String decodedImageName = URLDecoder.decode(imageName, StandardCharsets.UTF_8);
            
            // Construct the file path
            Path imagePath = Paths.get(PRODUCTS_BASE_PATH, decodedCategory, decodedProductName, decodedImageName);
            
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(imagePath.toFile());
            
            // Determine content type based on file extension
            String contentType = getContentType(imageName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}
