package com.donorbox.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.base.url:http://localhost}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    /**
     * Upload an image file to local storage
     * @param file The multipart file to upload
     * @param category The category/folder for the image (e.g., "causes", "events")
     * @return The relative path to access the uploaded image
     * @throws IOException if file upload fails
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = createUploadDirectory(category);
        
        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Copy file to upload directory
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Image uploaded successfully: {}", filePath.toString());
        
        // Return relative path for accessing the image
        return category + "/" + filename;
    }

    /**
     * Delete an image file from local storage
     * @param imagePath The relative path of the image to delete
     * @return true if file was deleted successfully
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path fullPath = Paths.get(uploadDir).resolve(imagePath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("Image deleted successfully: {}", fullPath.toString());
            } else {
                log.warn("Image file not found for deletion: {}", fullPath.toString());
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Error deleting image: {}", imagePath, e);
            return false;
        }
    }

    /**
     * Get the full URL for accessing an uploaded image
     * @param relativePath The relative path returned from uploadImage()
     * @return Full localhost URL to access the image
     */
    public String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        // Construct URL based on environment
        String port = serverPort.equals("8080") ? "" : ":" + serverPort;
        return baseUrl + port + "/api/images/" + relativePath;
    }

    /**
     * Check if a file exists in storage
     * @param relativePath The relative path of the image
     * @return true if file exists
     */
    public boolean imageExists(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(uploadDir).resolve(relativePath);
        return Files.exists(fullPath);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir).resolve(category);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toString());
            }
            
            // Test write permissions
            if (!Files.isWritable(uploadPath)) {
                throw new IOException("Upload directory is not writable: " + uploadPath.toString());
            }
            
        } catch (Exception e) {
            log.error("Failed to create or access upload directory: {}", uploadPath.toString(), e);
            throw new IOException("Cannot create upload directory: " + e.getMessage(), e);
        }
        
        return uploadPath;
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return timestamp + "_" + uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
