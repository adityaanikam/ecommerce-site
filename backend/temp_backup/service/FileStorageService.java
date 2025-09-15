package com.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Value("${app.upload.max-file-size}")
    private long maxFileSize;
    
    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;
    
    public List<String> storeProductImages(List<MultipartFile> files) {
        log.info("Storing {} product images", files.size());
        
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // Validate file
                validateFile(file);
                
                // Generate unique filename
                String fileName = generateUniqueFileName(file.getOriginalFilename());
                
                // Create upload directory if it doesn't exist
                Path uploadPath = Paths.get(uploadDir, "products");
                Files.createDirectories(uploadPath);
                
                // Copy file to upload directory
                Path targetLocation = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                
                // Generate URL
                String imageUrl = "/uploads/products/" + fileName;
                imageUrls.add(imageUrl);
                
                log.info("Successfully stored image: {}", imageUrl);
                
            } catch (IOException e) {
                log.error("Failed to store image: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to store image: " + file.getOriginalFilename(), e);
            }
        }
        
        return imageUrls;
    }
    
    public String storeUserAvatar(MultipartFile file) {
        log.info("Storing user avatar: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            validateFile(file);
            
            // Generate unique filename
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "avatars");
            Files.createDirectories(uploadPath);
            
            // Copy file to upload directory
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Generate URL
            String imageUrl = "/uploads/avatars/" + fileName;
            
            log.info("Successfully stored avatar: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Failed to store avatar: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store avatar: " + file.getOriginalFilename(), e);
        }
    }
    
    public String storeCategoryImage(MultipartFile file) {
        log.info("Storing category image: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            validateFile(file);
            
            // Generate unique filename
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "categories");
            Files.createDirectories(uploadPath);
            
            // Copy file to upload directory
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Generate URL
            String imageUrl = "/uploads/categories/" + fileName;
            
            log.info("Successfully stored category image: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Failed to store category image: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store category image: " + file.getOriginalFilename(), e);
        }
    }
    
    public void deleteFile(String fileUrl) {
        log.info("Deleting file: {}", fileUrl);
        
        try {
            // Extract file path from URL
            String filePath = fileUrl.replace("/uploads/", uploadDir + "/");
            Path path = Paths.get(filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Successfully deleted file: {}", fileUrl);
            } else {
                log.warn("File not found: {}", fileUrl);
            }
            
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size: " + maxFileSize);
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("File name is empty");
        }
        
        String extension = getFileExtension(fileName);
        if (!isAllowedExtension(extension)) {
            throw new RuntimeException("File extension not allowed: " + extension);
        }
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + "." + extension;
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    private boolean isAllowedExtension(String extension) {
        String[] allowed = allowedExtensions.split(",");
        for (String allowedExt : allowed) {
            if (allowedExt.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
