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
public class DocumentUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB for documents

    /**
     * Upload a document file to local storage
     * @param file The multipart file to upload
     * @param category The category/folder for the document (e.g., "proof-documents")
     * @return The relative path to access the uploaded document
     * @throws IOException if file upload fails
     */
    public String uploadDocument(MultipartFile file, String category) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = createUploadDirectory(category);
        
        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Copy file to upload directory
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Document uploaded successfully: {}", filePath.toString());
        
        // Return relative path for accessing the document
        return category + "/" + filename;
    }

    /**
     * Delete a document file from local storage
     * @param documentPath The relative path of the document to delete
     * @return true if file was deleted successfully
     */
    public boolean deleteDocument(String documentPath) {
        if (documentPath == null || documentPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path fullPath = Paths.get(uploadDir).resolve(documentPath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                log.info("Document deleted successfully: {}", fullPath.toString());
            } else {
                log.warn("Document file not found for deletion: {}", fullPath.toString());
            }
            
            return deleted;
        } catch (IOException e) {
            log.error("Error deleting document: {}", documentPath, e);
            return false;
        }
    }

    /**
     * Get the full URL for accessing an uploaded document
     * @param relativePath The relative path returned from uploadDocument()
     * @return Full URL to access the document
     */
    public String getDocumentUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        return "https://cloud-fund-i1kt.onrender.com" + serverPort + "/api/documents/" + relativePath;
    }

    /**
     * Check if a document file exists in storage
     * @param relativePath The relative path of the document
     * @return true if file exists
     */
    public boolean documentExists(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(uploadDir).resolve(relativePath);
        return Files.exists(fullPath);
    }

    /**
     * Get the file extension of a document
     * @param relativePath The relative path of the document
     * @return The file extension
     */
    public String getFileExtension(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = relativePath.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return relativePath.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Check if the file is a PDF document
     * @param relativePath The relative path of the document
     * @return true if file is PDF
     */
    public boolean isPdf(String relativePath) {
        return "pdf".equals(getFileExtension(relativePath));
    }

    /**
     * Check if the file is an image
     * @param relativePath The relative path of the document
     * @return true if file is an image
     */
    public boolean isImage(String relativePath) {
        String extension = getFileExtension(relativePath);
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(extension);
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

        String extension = extractFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(uploadDir).resolve(category);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toString());
        }
        
        return uploadPath;
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = extractFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return timestamp + "_" + uuid + "." + extension;
    }

    private String extractFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
