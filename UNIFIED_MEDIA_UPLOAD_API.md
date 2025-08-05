# Unified Media Upload API

## Overview

The unified media upload API combines separate image and video upload endpoints into a single API that automatically detects the file type and handles it accordingly. This simplifies the frontend implementation and provides a more consistent user experience.

## Key Features

- **Automatic File Type Detection**: The API automatically detects whether an uploaded file is an image or video based on file extension
- **Unified Response Format**: Consistent response structure regardless of media type
- **Support for Multiple Categories**: Organizes uploads by category (personal-causes, public-causes, causes, events, etc.)
- **File Validation**: Validates file types and sizes according to media type
- **Backward Compatibility**: Existing endpoints remain available but are marked as deprecated

## New Endpoints

### 1. Generic Media Upload
```
POST /api/upload-media
```
- **Description**: Upload any supported media file (image or video)
- **Parameters**: 
  - `file` (MultipartFile): Media file to upload
  - `category` (String, optional): Category for organizing media (default: "general")

### 2. Personal Cause Media Upload
```
POST /api/personal-causes/upload-media
```
- **Description**: Upload media for personal causes (auto-detects file type)
- **Parameters**: 
  - `file` (MultipartFile): Media file to upload (image or video)

### 3. Public Cause Media Upload
```
POST /api/public-causes/upload-media
```
- **Description**: Upload media for public causes (auto-detects file type)
- **Parameters**: 
  - `file` (MultipartFile): Media file to upload (image or video)

### 4. General Cause Media Upload
```
POST /api/causes/upload-media
```
- **Description**: Upload media for any cause (auto-detects file type)
- **Parameters**: 
  - `file` (MultipartFile): Media file to upload (image or video)

### 5. Personal Cause Submission with Media
```
POST /api/personal-cause-submissions/with-media
```
- **Description**: Submit a personal cause with unified media upload
- **Parameters**: All cause submission parameters plus:
  - `media` (MultipartFile, optional): Media file (image or video - auto-detected)

## Unified GET Endpoints

### 1. Generic Media Retrieval
```
GET /api/media/{category}/{filename}
```
- **Description**: Retrieve any uploaded media file (image or video)
- **Parameters**: 
  - `category` (String): Media category
  - `filename` (String): Media filename

### 2. Personal Cause Media Retrieval
```
GET /api/personal-causes/media/{filename}
```
- **Description**: Retrieve media file for a personal cause
- **Parameters**: 
  - `filename` (String): Media filename

### 3. Public Cause Media Retrieval
```
GET /api/public-causes/media/{filename}
```
- **Description**: Retrieve media file for a public cause
- **Parameters**: 
  - `filename` (String): Media filename

### 4. General Cause Media Retrieval
```
GET /api/causes/media/{filename}
```
- **Description**: Retrieve media file for any cause
- **Parameters**: 
  - `filename` (String): Media filename

### 5. Event Media Retrieval
```
GET /api/events/media/{filename}
```
- **Description**: Retrieve media file for an event
- **Parameters**: 
  - `filename` (String): Media filename

### 6. Blog Media Retrieval
```
GET /api/blogs/media/{filename}
```
- **Description**: Retrieve media file for a blog
- **Parameters**: 
  - `filename` (String): Media filename

## Response Format

All unified media upload endpoints return a consistent JSON response:

```json
{
  "message": "Media uploaded successfully",
  "mediaType": "IMAGE" | "VIDEO",
  "mediaPath": "category/filename",
  "mediaUrl": "https://cloud-fund-i1kt.onrender.com/api/media/category/filename",
  "filename": "original_filename.ext",
  "category": "category_name"
}
```

## Supported File Types

### Images
- jpg, jpeg
- png
- gif
- webp
- bmp
- tiff
- svg

### Videos
- mp4
- avi
- mov
- wmv
- flv
- webm
- mkv
- m4v
- 3gp
- ogv

## File Size Limits

- **Images**: Maximum 10MB
- **Videos**: Maximum 100MB

## Backend Implementation

### MediaUploadService Methods

- `uploadMedia(MultipartFile file, String category)`: Unified upload method
- `isImageFile(String filename)`: Detects if file is an image
- `isVideoFile(String filename)`: Detects if file is a video
- `getMediaType(String filename)`: Returns MediaType enum (IMAGE/VIDEO)
- `getContentType(String filename)`: Returns appropriate MIME type

### Validation

The service automatically validates:
- File is not empty
- File extension is supported
- File size is within limits for the detected media type
- Creates upload directories if they don't exist

## Migration Guide

## GET Endpoint Features

- **Automatic Content Type Detection**: Serves files with correct MIME types
- **Optimized Caching**: 
  - Images: 1 year cache (max-age=31536000)
  - Videos: 1 hour cache (max-age=3600)
- **Video Streaming Support**: Includes `Accept-Ranges: bytes` header for video files
- **Inline Display**: Files are served with `inline` disposition for browser display
- **Error Handling**: Returns 404 for missing files with proper logging

### Usage Examples

**Retrieving Media:**
```javascript
// Generic endpoint (requires category)
fetch('/api/media/personal-causes/20240805_12345678.jpg')

// Category-specific endpoints (more convenient)
fetch('/api/personal-causes/media/20240805_12345678.jpg')
fetch('/api/causes/media/20240805_87654321.mp4')
fetch('/api/events/media/event_video.webm')
```

**In HTML:**
```html
<!-- For images -->
<img src="/api/personal-causes/media/cause_image.jpg" alt="Cause Image" />

<!-- For videos -->
<video controls>
  <source src="/api/causes/media/cause_video.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video>
```

### For Frontend Developers

**Before (Multiple Endpoints):**
```javascript
// For images
const formData = new FormData();
formData.append('image', imageFile);
fetch('/api/personal-causes/upload-image', {
  method: 'POST',
  body: formData
});

// For videos
const formData2 = new FormData();
formData2.append('video', videoFile);
fetch('/api/causes/upload-video', {
  method: 'POST',
  body: formData2
});
```

**After (Unified Endpoint):**
```javascript
// For any media type
const formData = new FormData();
formData.append('file', mediaFile); // Can be image or video
fetch('/api/personal-causes/upload-media', {
  method: 'POST',
  body: formData
});

// Or use the submission endpoint
const submissionData = new FormData();
submissionData.append('media', mediaFile);
submissionData.append('title', 'Cause Title');
// ... other fields
fetch('/api/personal-cause-submissions/with-media', {
  method: 'POST',
  body: submissionData
});
```

### Backward Compatibility

- Existing endpoints remain functional but are marked as `@Deprecated`
- Legacy endpoints: `/api/personal-cause-submissions/with-image`
- Recommended to migrate to new unified endpoints for consistency

## Testing

The implementation includes comprehensive tests for:
- File type detection (images and videos)
- Media type classification
- Content type determination
- URL generation
- Edge cases (null, empty, invalid files)

Run tests with:
```bash
mvn test
```

## Benefits

1. **Simplified Frontend Code**: Single endpoint handles both images and videos
2. **Automatic Detection**: No need to determine file type on frontend
3. **Consistent API**: Uniform response format across all media uploads
4. **Better UX**: Users can upload any supported media without selecting type
5. **Maintainability**: Centralized media handling logic
6. **Future-Proof**: Easy to add new media types in the future
