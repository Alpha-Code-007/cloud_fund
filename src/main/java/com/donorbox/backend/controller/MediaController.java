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

    // =================== UNIFIED CAUSE MEDIA ENDPOINTS ===================

    @PostMapping("/personal-causes/upload-media")
    @Operation(summary = "Upload media for personal cause", description = "Upload image or video file for a personal cause (auto-detects file type)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadPersonalCauseMedia(
            @Parameter(description = "Media file to upload (image or video)")
            @RequestParam("file") MultipartFile file) {
        
        return uploadMedia(file, "personal-causes");
    }

    @PostMapping("/public-causes/upload-media")
    @Operation(summary = "Upload media for public cause", description = "Upload image or video file for a public cause (auto-detects file type)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadPublicCauseMedia(
            @Parameter(description = "Media file to upload (image or video)")
            @RequestParam("file") MultipartFile file) {
        
        return uploadMedia(file, "public-causes");
    }

    @PostMapping("/causes/upload-media")
    @Operation(summary = "Upload media for cause", description = "Upload image or video file for any cause (auto-detects file type)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "413", description = "File size too large")
    })
    public ResponseEntity<Map<String, String>> uploadCauseMedia(
            @Parameter(description = "Media file to upload (image or video)")
            @RequestParam("file") MultipartFile file) {
        
        return uploadMedia(file, "causes");
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
        
        return serveMediaFile(category, filename);
    }

    // =================== UNIFIED CAUSE MEDIA RETRIEVAL ENDPOINTS ===================

    @GetMapping("/personal-causes/media/{filename:.+}")
    @Operation(summary = "Get personal cause media", description = "Retrieve media file for a personal cause (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getPersonalCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return serveMediaFile("personal-causes", filename);
    }

    @GetMapping("/public-causes/media/{filename:.+}")
    @Operation(summary = "Get public cause media", description = "Retrieve media file for a public cause (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getPublicCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return serveMediaFile("public-causes", filename);
    }

    @GetMapping("/causes/media/{filename:.+}")
    @Operation(summary = "Get cause media", description = "Retrieve media file for any cause (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return serveMediaFile("causes", filename);
    }

    @GetMapping("/events/media/{filename:.+}")
    @Operation(summary = "Get event media", description = "Retrieve media file for an event (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getEventMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return serveMediaFile("events", filename);
    }

    @GetMapping("/blogs/media/{filename:.+}")
    @Operation(summary = "Get blog media", description = "Retrieve media file for a blog (image or video)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Resource> getBlogMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return serveMediaFile("blogs", filename);
    }

    // Note: Image endpoints are handled by ImageController to avoid mapping conflicts

    // =================== MEDIA UPDATE ENDPOINTS ===================

    @PutMapping("/media/{category}/{filename:.+}")
    @Operation(summary = "Update/Replace uploaded media", description = "Replace an existing media file with a new one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updateMedia(
            @Parameter(description = "Media category")
            @PathVariable String category,
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file to replace the existing one")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile(category, filename, newFile);
    }

    @PutMapping("/personal-causes/media/{filename:.+}")
    @Operation(summary = "Update personal cause media", description = "Replace media file for a personal cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updatePersonalCauseMedia(
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile("personal-causes", filename, newFile);
    }

    @PutMapping("/public-causes/media/{filename:.+}")
    @Operation(summary = "Update public cause media", description = "Replace media file for a public cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updatePublicCauseMedia(
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile("public-causes", filename, newFile);
    }

    @PutMapping("/causes/media/{filename:.+}")
    @Operation(summary = "Update cause media", description = "Replace media file for any cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updateCauseMedia(
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile("causes", filename, newFile);
    }

    @PutMapping("/events/media/{filename:.+}")
    @Operation(summary = "Update event media", description = "Replace media file for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updateEventMedia(
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile("events", filename, newFile);
    }

    @PutMapping("/blogs/media/{filename:.+}")
    @Operation(summary = "Update blog media", description = "Replace media file for a blog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media updated successfully"),
            @ApiResponse(responseCode = "404", description = "Original media not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file or update failed")
    })
    public ResponseEntity<Map<String, String>> updateBlogMedia(
            @Parameter(description = "Original media filename")
            @PathVariable String filename,
            @Parameter(description = "New media file")
            @RequestParam("file") MultipartFile newFile) {
        
        return updateMediaFile("blogs", filename, newFile);
    }

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
        
        return deleteMediaFile(category, filename);
    }

    @DeleteMapping("/personal-causes/media/{filename:.+}")
    @Operation(summary = "Delete personal cause media", description = "Delete media file for a personal cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Map<String, String>> deletePersonalCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return deleteMediaFile("personal-causes", filename);
    }

    @DeleteMapping("/public-causes/media/{filename:.+}")
    @Operation(summary = "Delete public cause media", description = "Delete media file for a public cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Map<String, String>> deletePublicCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return deleteMediaFile("public-causes", filename);
    }

    @DeleteMapping("/causes/media/{filename:.+}")
    @Operation(summary = "Delete cause media", description = "Delete media file for any cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Map<String, String>> deleteCauseMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return deleteMediaFile("causes", filename);
    }

    @DeleteMapping("/events/media/{filename:.+}")
    @Operation(summary = "Delete event media", description = "Delete media file for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Map<String, String>> deleteEventMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return deleteMediaFile("events", filename);
    }

    @DeleteMapping("/blogs/media/{filename:.+}")
    @Operation(summary = "Delete blog media", description = "Delete media file for a blog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<Map<String, String>> deleteBlogMedia(
            @Parameter(description = "Media filename")
            @PathVariable String filename) {
        
        return deleteMediaFile("blogs", filename);
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

    private ResponseEntity<Resource> serveMediaFile(String category, String filename) {
        try {
            Path mediaPath = Paths.get(uploadDir).resolve(category).resolve(filename);
            Resource resource = new UrlResource(mediaPath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = mediaUploadService.getContentType(filename);
                
                // Set appropriate headers for different media types
                ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType));
                
                // For images, use inline disposition; for videos, allow inline as well
                if (mediaUploadService.isImageFile(filename)) {
                    responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
                    // Add cache headers for images
                    responseBuilder.header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000"); // 1 year
                } else if (mediaUploadService.isVideoFile(filename)) {
                    responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
                    // Add headers for video streaming
                    responseBuilder.header(HttpHeaders.ACCEPT_RANGES, "bytes");
                    responseBuilder.header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600"); // 1 hour
                }
                
                return responseBuilder.body(resource);
            } else {
                log.warn("Media not found or not readable: {}", mediaPath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Error creating resource for media: {}/{}", category, filename, e);
            return ResponseEntity.notFound().build();
        }
    }

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
            // Validate input parameters
            if (file == null) {
                response.put("error", "No file provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (file.isEmpty()) {
                response.put("error", "Please select a media file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (category == null || category.trim().isEmpty()) {
                response.put("error", "Category is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Starting media upload: file={}, size={}, category={}", 
                    file.getOriginalFilename(), file.getSize(), category);
            
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
            response.put("fileSize", String.valueOf(file.getSize()));
            
            log.info("Media uploaded successfully: {} -> {}", file.getOriginalFilename(), relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("IOException during media upload: {}", e.getMessage(), e);
            response.put("error", "Failed to upload media: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Unexpected error during media upload: {}", e.getMessage(), e);
            response.put("error", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, String>> updateMediaFile(String category, String filename, MultipartFile newFile) {
        Map<String, String> response = new HashMap<>();
        
        String oldMediaPath = category + "/" + filename;
        
        // Check if the original file exists
        if (!mediaUploadService.mediaExists(oldMediaPath)) {
            response.put("error", "Original media file not found");
            response.put("mediaPath", oldMediaPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        try {
            if (newFile.isEmpty()) {
                response.put("error", "Please select a media file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Delete the old file first
            boolean deleted = mediaUploadService.deleteMedia(oldMediaPath);
            if (!deleted) {
                log.warn("Failed to delete old media file: {}", oldMediaPath);
            }
            
            // Upload the new file
            String relativePath = mediaUploadService.uploadMedia(newFile, category);
            String fullUrl = mediaUploadService.getMediaUrl(relativePath);
            
            // Determine media type
            String mediaType = "UNKNOWN";
            if (mediaUploadService.isImageFile(newFile.getOriginalFilename())) {
                mediaType = "IMAGE";
            } else if (mediaUploadService.isVideoFile(newFile.getOriginalFilename())) {
                mediaType = "VIDEO";
            }
            
            response.put("message", "Media updated successfully");
            response.put("oldMediaPath", oldMediaPath);
            response.put("newMediaPath", relativePath);
            response.put("mediaUrl", fullUrl);
            response.put("filename", newFile.getOriginalFilename());
            response.put("category", category);
            response.put("mediaType", mediaType);
            
            log.info("Media updated successfully: {} -> {}", oldMediaPath, relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error updating media: {}", e.getMessage(), e);
            response.put("error", "Failed to update media: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, String>> deleteMediaFile(String category, String filename) {
        String mediaPath = category + "/" + filename;
        boolean deleted = mediaUploadService.deleteMedia(mediaPath);
        
        Map<String, String> response = new HashMap<>();
        
        if (deleted) {
            response.put("message", "Media deleted successfully");
            response.put("mediaPath", mediaPath);
            log.info("Media deleted successfully: {}", mediaPath);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Media not found or could not be deleted");
            response.put("mediaPath", mediaPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
