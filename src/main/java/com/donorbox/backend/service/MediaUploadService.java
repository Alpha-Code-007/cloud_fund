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
public class MediaUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.base.url:http://localhost}")
    private String baseUrl;

    // Image file extensions
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff", "svg"
    );

    // Video file extensions
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList(
        "mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v", "3gp", "ogv"
    );

    // File size limits
    private static final long MAX_IMAGE_SIZE = 25 * 1024 * 1024; // 25MB for images
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB for videos

    /**
     * Upload a media file (image or video) to local storage
     * @param file The multipart file to upload
     * @param category The category/folder for the media (e.g., "causes", "events")
     * @return The relative path to access the uploaded media
     * @throws IOException if file upload fails
     */
    public String uploadMedia(MultipartFile file, String category) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = createUploadDirectory(category);
        
        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Copy file to upload directory
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Media uploaded successfully: {}", filePath.toString());
        
        // Return relative path for accessing the media
        return category + "/" + filename;
    }

    /**
     * Upload an image file specifically
     * @param file The image file to upload
     * @param category The category for organizing images
     * @return The relative path to access the uploaded image
     * @throws IOException if file upload fails
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        validateImageFile(file);
        return uploadMedia(file, category);
    }

    /**
     * Upload a video file specifically
     * @param file The video file to upload
     * @param category The category for organizing videos
     * @return The relative path to access the uploaded video
     * @throws IOException if file upload fails
     */
    public String uploadVideo(MultipartFile file, String category) throws IOException {
        validateVideoFile(file);
        return uploadMedia(file, category);
    }

    /**
     * Delete a media file from local storage
     * @param mediaPath The relative path of the media to delete
     * @return true if file was deleted successfully
     */
    public boolean deleteMedia(String mediaPath) {
        if (mediaPath == null || mediaPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path fullPath = Paths.get(uploadDir).resolve(mediaPath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("Media deleted successfully: {}", fullPath.toString());
            } else {
                log.warn("Media file not found for deletion: {}", fullPath.toString());
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Error deleting media: {}", mediaPath, e);
            return false;
        }
    }

    /**
     * Get the full URL for accessing uploaded media
     * @param relativePath The relative path returned from uploadMedia()
     * @return Full URL to access the media
     */
    public String getMediaUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        // Build URL dynamically based on configuration
        String port = serverPort.equals("8080") ? "" : ":" + serverPort;
        return baseUrl + port + "/api/media/" + relativePath;
    }

    /**
     * Check if a media file exists in storage
     * @param relativePath The relative path of the media
     * @return true if file exists
     */
    public boolean mediaExists(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(uploadDir).resolve(relativePath);
        return Files.exists(fullPath);
    }

    /**
     * Determine if a file is an image based on its extension
     * @param filename The filename to check
     * @return true if the file is an image
     */
    public boolean isImageFile(String filename) {
        if (filename == null) return false;
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Determine if a file is a video based on its extension
     * @param filename The filename to check
     * @return true if the file is a video
     */
    public boolean isVideoFile(String filename) {
        if (filename == null) return false;
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * Get the media type based on file extension
     * @param filename The filename to check
     * @return FileMediaType enum (IMAGE, VIDEO, or null if unknown)
     */
    public FileMediaType getMediaType(String filename) {
        if (isImageFile(filename)) {
            return FileMediaType.IMAGE;
        } else if (isVideoFile(filename)) {
            return FileMediaType.VIDEO;
        }
        return null;
    }

    /**
     * Get content type for serving media files
     * @param filename The filename to determine content type for
     * @return Content type string
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        // Image content types
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "tiff":
                return "image/tiff";
            case "svg":
                return "image/svg+xml";
                
            // Video content types
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            case "mkv":
                return "video/x-matroska";
            case "m4v":
                return "video/x-m4v";
            case "3gp":
                return "video/3gpp";
            case "ogv":
                return "video/ogg";
                
            default:
                return "application/octet-stream";
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty or null");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        boolean isImage = ALLOWED_IMAGE_EXTENSIONS.contains(extension);
        boolean isVideo = ALLOWED_VIDEO_EXTENSIONS.contains(extension);

        if (!isImage && !isVideo) {
            throw new IOException("File type not allowed. Allowed types: " + 
                String.join(", ", ALLOWED_IMAGE_EXTENSIONS) + ", " + 
                String.join(", ", ALLOWED_VIDEO_EXTENSIONS));
        }

        // Check file size based on type
        if (isImage && file.getSize() > MAX_IMAGE_SIZE) {
            throw new IOException("Image size exceeds maximum allowed size of " + 
                (MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }

        if (isVideo && file.getSize() > MAX_VIDEO_SIZE) {
            throw new IOException("Video size exceeds maximum allowed size of " + 
                (MAX_VIDEO_SIZE / 1024 / 1024) + "MB");
        }
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Image file is empty or null");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IOException("Image size exceeds maximum allowed size of " + 
                (MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Image file name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IOException("Image type not allowed. Allowed types: " + 
                String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }

    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Video file is empty or null");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IOException("Video size exceeds maximum allowed size of " + 
                (MAX_VIDEO_SIZE / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Video file name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new IOException("Video type not allowed. Allowed types: " + 
                String.join(", ", ALLOWED_VIDEO_EXTENSIONS));
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

    public enum FileMediaType {
        IMAGE, VIDEO
    }
}
