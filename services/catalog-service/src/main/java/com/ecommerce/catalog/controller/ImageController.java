package com.ecommerce.catalog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Serves product images off the filesystem, as the monolith did.
 *
 * Two things changed from the monolith, both deliberate:
 *  - the base directory is configurable instead of being the hardcoded container
 *    path /app/Products/, which was wrong in every environment except the image;
 *  - the resolved path is checked to be inside the base directory before the file is
 *    read. The monolith URL-decoded the path variables and concatenated them with no
 *    check, so an encoded ../ could walk out of the image directory.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final Path baseDirectory;

    public ImageController(@Value("${catalog.images.base-path:./Products}") String basePath) {
        this.baseDirectory = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Image controller is working!");
    }

    @GetMapping("/product/{category}/{productName}/{imageName}")
    public ResponseEntity<Resource> getProductImage(
            @PathVariable String category,
            @PathVariable String productName,
            @PathVariable String imageName) {

        Path imagePath;
        try {
            imagePath = baseDirectory
                    .resolve(decode(category))
                    .resolve(decode(productName))
                    .resolve(decode(imageName))
                    .normalize();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // Refuse anything that normalises to a location outside the image directory.
        if (!imagePath.startsWith(baseDirectory)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(imagePath) || Files.isDirectory(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType(imageName)))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                    .contentLength(Files.size(imagePath))
                    .body(new FileSystemResource(imagePath));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String contentType(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
