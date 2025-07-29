package com.donorbox.backend.controller;

import com.donorbox.backend.service.MediaUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media API", description = "Endpoints for media (image and video) upload and retrieval")
public class MediaController {

    private final MediaUploadService mediaUploadService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


    // =================== VIDEO UPLOAD ENDPOINTS ===================

    @PostMapping("/causes/upload-video")
    @Operation(summary = "Upload cause video", description = "Upload a video file for a cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadCauseVideo(
            @Parameter(description = "Video file to upload")
            @RequestParam("video") MultipartFile file) {
        
        return uploadVideo(file, "causes");
    }

    @PostMapping("/events/upload-video")
    @Operation(summary = "Upload event video", description = "Upload a video file for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadEventVideo(
            @Parameter(description = "Video file to upload")
            @RequestParam("video") MultipartFile file) {
        
        return uploadVideo(file, "events");
    }

    // =================== GENERIC MEDIA ENDPOINTS ===================

    @PostMapping("/upload-media")
    @Operation(summary = "Upload media file", description = "Upload any supported media file (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadGenericMedia(
            @Parameter(description = "Media file to upload")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Category for organizing media")
            @RequestParam(value = "category", defaultValue = "general") String category) {
        
        return uploadMedia(file, category);
    }

    // =================== MEDIA SERVING ENDPOINTS ===================

    @GetMapping("/media/{category}/{filename:.+}")
    @Operation(summary = "Get uploaded media", description = "Retrieve an uploaded media file (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getMedia(
            @Parameter(description = "Media category")
            @PathVariable String category,
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        try {
            Path mediaPath = Paths.get(uploadDir).resolve(category).resolve(filename);
            Resource resource = new UrlResource(mediaPath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = mediaUploadService.getContentType(filename);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                log.warn("Media not found or not readable: {}", mediaPath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Error creating resource for media: {}/{}", category, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    // Note: Image endpoints are handled by ImageController to avoid mapping conflicts

    // =================== MEDIA DELETION ENDPOINTS ===================

    @DeleteMapping("/media/{category}/{filename:.+}")
    @Operation(summary = "Delete uploaded media", description = "Delete an uploaded media file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found"),
            @ApiResponse(responseCode = "500", description = "Error deleting media")
    })
    public ResponseEntity<Map<String, String>> deleteMedia(
            @Parameter(description = "Media category")
            @PathVariable String category,
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        String mediaPath = category + "/" + filename;
        boolean deleted = mediaUploadService.deleteMedia(mediaPath);
        
        Map<String, String> response = new HashMap<>();
        
        if (deleted) {
            response.put("message", "Media deleted successfully");
            response.put("mediaPath", mediaPath);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Media not found or could not be deleted");
            response.put("mediaPath", mediaPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // =================== MEDIA INFO ENDPOINTS ===================

    @GetMapping("/media/info/{category}/{filename:.+}")
    @Operation(summary = "Get media information", description = "Retrieve information about an uploaded media file")
    public ResponseEntity<Map<String, Object>> getMediaInfo(
            @Parameter(description = "Media category")
            @PathVariable String category,
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        String mediaPath = category + "/" + filename;
        Map<String, Object> response = new HashMap<>();
        
        if (mediaUploadService.mediaExists(mediaPath)) {
            response.put("exists", true);
            response.put("mediaPath", mediaPath);
            response.put("mediaUrl", mediaUploadService.getMediaUrl(mediaPath));
            response.put("isImage", mediaUploadService.isImageFile(filename));
            response.put("isVideo", mediaUploadService.isVideoFile(filename));
            response.put("mediaType", mediaUploadService.getMediaType(filename));
            response.put("contentType", mediaUploadService.getContentType(filename));
            
            return ResponseEntity.ok(response);
        } else {
            response.put("exists", false);
            response.put("mediaPath", mediaPath);
            return ResponseEntity.notFound().build();
        }
    }

    // =================== PRIVATE HELPER METHODS ===================

    private ResponseEntity<Map<String, String>> uploadImage(MultipartFile file, String category) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "Please select an image file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            String relativePath = mediaUploadService.uploadImage(file, category);
            String fullUrl = mediaUploadService.getMediaUrl(relativePath);
            
            response.put("message", "Image uploaded successfully");
            response.put("mediaPath", relativePath);
            response.put("mediaUrl", fullUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("category", category);
            response.put("mediaType", "IMAGE");
            
            log.info("Image uploaded successfully: {} -> {}", file.getOriginalFilename(), relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, String>> uploadVideo(MultipartFile file, String category) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "Please select a video file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            String relativePath = mediaUploadService.uploadVideo(file, category);
            String fullUrl = mediaUploadService.getMediaUrl(relativePath);
            
            response.put("message", "Video uploaded successfully");
            response.put("mediaPath", relativePath);
            response.put("mediaUrl", fullUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("category", category);
            response.put("mediaType", "VIDEO");
            
            log.info("Video uploaded successfully: {} -> {}", file.getOriginalFilename(), relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading video: {}", e.getMessage(), e);
            response.put("error", "Failed to upload video: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, String>> uploadMedia(MultipartFile file, String category) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "Please select a media file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            String relativePath = mediaUploadService.uploadMedia(file, category);
            String fullUrl = mediaUploadService.getMediaUrl(relativePath);
            
            // Determine media type
            String mediaType = "UNKNOWN";
            if (mediaUploadService.isImageFile(file.getOriginalFilename())) {
                mediaType = "IMAGE";
            } else if (mediaUploadService.isVideoFile(file.getOriginalFilename())) {
                mediaType = "VIDEO";
            }
            
            response.put("message", "Media uploaded successfully");
            response.put("mediaPath", relativePath);
            response.put("mediaUrl", fullUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("category", category);
            response.put("mediaType", mediaType);
            
            log.info("Media uploaded successfully: {} -> {}", file.getOriginalFilename(), relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading media: {}", e.getMessage(), e);
            response.put("error", "Failed to upload media: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
