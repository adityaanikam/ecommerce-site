package com.ecommerce.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    
    private String id;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;
    
    @NotBlank(message = "File path is required")
    private String filePath;
    
    @NotBlank(message = "Content type is required")
    private String contentType;
    
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private Boolean isActive;
    
    // Inner classes for request/response DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String id;
        private String fileName;
        private String filePath;
        private String contentType;
        private Long fileSize;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
        private String downloadUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileUploadRequest {
        @NotBlank(message = "File name is required")
        @Size(max = 255, message = "File name cannot exceed 255 characters")
        private String fileName;
        
        @NotBlank(message = "Content type is required")
        private String contentType;
        
        private Long fileSize;
    }
}
