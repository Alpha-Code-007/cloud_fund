package com.donorbox.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.upload.dir=test-uploads"
})
class MediaUploadServiceTest {

    private MediaUploadService mediaUploadService;

    @BeforeEach
    void setUp() {
        mediaUploadService = new MediaUploadService();
    }

    @Test
    void testIsImageFile() {
        // Test image file detection
        assertTrue(mediaUploadService.isImageFile("test.jpg"));
        assertTrue(mediaUploadService.isImageFile("test.jpeg"));
        assertTrue(mediaUploadService.isImageFile("test.png"));
        assertTrue(mediaUploadService.isImageFile("test.gif"));
        assertTrue(mediaUploadService.isImageFile("test.webp"));
        
        // Test non-image files
        assertFalse(mediaUploadService.isImageFile("test.mp4"));
        assertFalse(mediaUploadService.isImageFile("test.avi"));
        assertFalse(mediaUploadService.isImageFile("test.txt"));
        assertFalse(mediaUploadService.isImageFile(null));
    }

    @Test
    void testIsVideoFile() {
        // Test video file detection
        assertTrue(mediaUploadService.isVideoFile("test.mp4"));
        assertTrue(mediaUploadService.isVideoFile("test.avi"));
        assertTrue(mediaUploadService.isVideoFile("test.mov"));
        assertTrue(mediaUploadService.isVideoFile("test.wmv"));
        assertTrue(mediaUploadService.isVideoFile("test.webm"));
        
        // Test non-video files
        assertFalse(mediaUploadService.isVideoFile("test.jpg"));
        assertFalse(mediaUploadService.isVideoFile("test.png"));
        assertFalse(mediaUploadService.isVideoFile("test.txt"));
        assertFalse(mediaUploadService.isVideoFile(null));
    }

    @Test
    void testGetMediaType() {
        // Test image media type
        assertEquals(MediaUploadService.FileMediaType.IMAGE, mediaUploadService.getMediaType("test.jpg"));
        assertEquals(MediaUploadService.FileMediaType.IMAGE, mediaUploadService.getMediaType("test.png"));
        
        // Test video media type
        assertEquals(MediaUploadService.FileMediaType.VIDEO, mediaUploadService.getMediaType("test.mp4"));
        assertEquals(MediaUploadService.FileMediaType.VIDEO, mediaUploadService.getMediaType("test.avi"));
        
        // Test unknown media type
        assertNull(mediaUploadService.getMediaType("test.txt"));
        assertNull(mediaUploadService.getMediaType(null));
    }

    @Test
    void testGetContentType() {
        // Test image content types
        assertEquals("image/jpeg", mediaUploadService.getContentType("test.jpg"));
        assertEquals("image/jpeg", mediaUploadService.getContentType("test.jpeg"));
        assertEquals("image/png", mediaUploadService.getContentType("test.png"));
        assertEquals("image/gif", mediaUploadService.getContentType("test.gif"));
        assertEquals("image/webp", mediaUploadService.getContentType("test.webp"));
        
        // Test video content types
        assertEquals("video/mp4", mediaUploadService.getContentType("test.mp4"));
        assertEquals("video/x-msvideo", mediaUploadService.getContentType("test.avi"));
        assertEquals("video/quicktime", mediaUploadService.getContentType("test.mov"));
        assertEquals("video/webm", mediaUploadService.getContentType("test.webm"));
        
        // Test unknown content type
        assertEquals("application/octet-stream", mediaUploadService.getContentType("test.txt"));
    }

    @Test
    void testGetMediaUrl() {
        String relativePath = "personal-causes/test.jpg";
        String expectedUrl = "http://localhost:8080/api/media/" + relativePath;
        
        assertEquals(expectedUrl, mediaUploadService.getMediaUrl(relativePath));
        assertNull(mediaUploadService.getMediaUrl(null));
        assertNull(mediaUploadService.getMediaUrl(""));
        assertNull(mediaUploadService.getMediaUrl("   "));
    }
}
