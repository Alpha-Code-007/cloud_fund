# Personal Cause Submissions API - Enhanced Media Support

The Personal Cause Submissions API now supports comprehensive media file uploads including both **images** and **videos**. This document outlines all available endpoints and how to use them.

## Base URL
```
http://localhost:8080/api/personal-cause-submissions
```

## Supported File Types

### Images
- **Extensions**: JPG, JPEG, PNG, GIF, WEBP, BMP, TIFF, SVG
- **Max Size**: 10MB per file
- **Purpose**: Cause cover images, proof images

### Videos  
- **Extensions**: MP4, AVI, MOV, WMV, FLV, WEBM, MKV, M4V, 3GP, OGV
- **Max Size**: 100MB per file
- **Purpose**: Cause promotional videos, proof videos

## API Endpoints

### 1. Submit with Full Media Support (RECOMMENDED)
**POST** `/submit`
- **Content-Type**: `multipart/form-data`
- **Description**: Submit a personal cause with separate image, video, and proof document support
- **Supports**: Image + Video + Proof Document in single request

#### Form Parameters:
```
title (required): string
description (required): string  
shortDescription: string
targetAmount (required): decimal
category: string
location: string
endDate: string (ISO format)
submitterName (required): string
submitterEmail (required): string (valid email)
submitterPhone: string
submitterMessage: string
image: file (optional) - Image file for the cause
video: file (optional) - Video file for the cause
proofDocument: file (optional) - Proof document (PDF, DOC, etc.)
```

#### Example cURL:
```bash
curl -X POST "http://localhost:8080/api/personal-cause-submissions/submit" \
  -H "Content-Type: multipart/form-data" \
  -F "title=Help Build School in Rural Area" \
  -F "description=We need funds to build a school in a remote village" \
  -F "targetAmount=50000" \
  -F "category=Education" \
  -F "location=Rural Village, State" \
  -F "submitterName=John Doe" \
  -F "submitterEmail=john.doe@example.com" \
  -F "submitterPhone=+1234567890" \
  -F "image=@school_photo.jpg" \
  -F "video=@school_video.mp4" \
  -F "proofDocument=@land_certificate.pdf"
```

### 2. Submit with Auto-Detecting Media + Documents ⭐ ENHANCED
**POST** `/with-media`
- **Content-Type**: `multipart/form-data`
- **Description**: Submit with a single media file (auto-detects if image/video) + optional proof documents
- **Supports**: Single media file (image OR video) + Proof documents
- **NEW**: Now includes document support!

#### Form Parameters:
```
title (required): string
description (required): string
targetAmount (required): decimal
submitterName (required): string
submitterEmail (required): string
media: file (optional) - Will auto-detect if image or video
proofDocument: file (optional) - Proof document (PDF, DOC, DOCX, etc.)
[... other optional fields]
```

#### Example cURL:
```bash
curl -X POST "http://localhost:8080/api/personal-cause-submissions/with-media" \
  -H "Content-Type: multipart/form-data" \
  -F "title=Medical Emergency Fund" \
  -F "description=Urgent medical treatment needed" \
  -F "targetAmount=25000" \
  -F "submitterName=Jane Smith" \
  -F "submitterEmail=jane.smith@example.com" \
  -F "media=@medical_report.jpg" \
  -F "proofDocument=@medical_certificate.pdf"
```

### 3. Legacy: Submit with Image and Video (Deprecated)
**POST** `/with-image`
- **Status**: Deprecated (use `/submit` instead)
- **Content-Type**: `multipart/form-data`
- **Supports**: Separate image and video fields

### 4. Submit with Files (Image + Proof Document)
**POST** `/with-files`
- **Content-Type**: `multipart/form-data`
- **Description**: Submit with image and proof document (no video)
- **Supports**: Image + Proof Document

### 5. JSON Only Submission
**POST** `/`
- **Content-Type**: `application/json`
- **Description**: Submit cause data without any file uploads
- **Use Case**: When media files are uploaded separately

## Response Format

All endpoints return a `PersonalCauseSubmissionResponse` object:

```json
{
  "id": 123,
  "title": "Help Build School in Rural Area",
  "description": "We need funds to build a school...",
  "targetAmount": 50000.00,
  "imageUrl": "http://localhost:8080/api/images/personal-causes/20241203_143022_a1b2c3d4.jpg",
  "videoUrl": "http://localhost:8080/api/media/personal-causes/20241203_143023_e5f6g7h8.mp4",
  "proofDocumentUrl": "http://localhost:8080/api/documents/proof-documents/20241203_143024_i9j0k1l2.pdf",
  "submitterName": "John Doe",
  "submitterEmail": "john.doe@example.com",
  "status": "PENDING",
  "createdAt": "2024-12-03T14:30:22",
  "updatedAt": "2024-12-03T14:30:22"
}
```

## Media Access URLs

Once uploaded, media files can be accessed via:

### Images
```
GET /api/images/personal-causes/{filename}
```

### Videos  
```
GET /api/media/personal-causes/{filename}
```

### Documents
```
GET /api/documents/proof-documents/{filename}
```

## Error Responses

### File Upload Errors
```json
{
  "error": "File size exceeds maximum allowed size of 10MB",
  "timestamp": "2024-12-03T14:30:22"
}
```

### Validation Errors
```json
{
  "error": "File type not allowed. Allowed types: jpg, jpeg, png, gif, webp, mp4, avi, mov",
  "timestamp": "2024-12-03T14:30:22"
}
```

## Best Practices

1. **Use `/submit` endpoint** for new implementations - it provides the most comprehensive support
2. **Validate file types** on the client side before uploading
3. **Compress large videos** before uploading to stay under 100MB limit
4. **Optimize images** for web (recommend under 2MB for better performance)
5. **Provide meaningful filenames** to help with organization
6. **Handle upload progress** for large files to improve user experience

## Integration Examples

### JavaScript/Fetch API
```javascript
const formData = new FormData();
formData.append('title', 'My Cause Title');
formData.append('description', 'Detailed description...');
formData.append('targetAmount', '10000');
formData.append('submitterName', 'John Doe');
formData.append('submitterEmail', 'john@example.com');
formData.append('image', imageFile); // File object
formData.append('video', videoFile); // File object

fetch('http://localhost:8080/api/personal-cause-submissions/submit', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log('Success:', data))
.catch(error => console.error('Error:', error));
```

### React with Axios
```javascript
import axios from 'axios';

const submitCause = async (formData) => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/personal-cause-submissions/submit',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          );
          console.log(`Upload Progress: ${percentCompleted}%`);
        },
      }
    );
    return response.data;
  } catch (error) {
    console.error('Upload failed:', error);
    throw error;
  }
};
```

## Migration Guide

If you're currently using the basic `/api/personal-cause-submissions` endpoint:

**Before (JSON only):**
```javascript
// Only text data, no files
const response = await fetch('/api/personal-cause-submissions', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(causeData)
});
```

**After (With media support):**
```javascript
// Text data + image + video + documents
const formData = new FormData();
Object.keys(causeData).forEach(key => {
  formData.append(key, causeData[key]);
});
formData.append('image', imageFile);
formData.append('video', videoFile);

const response = await fetch('/api/personal-cause-submissions/submit', {
  method: 'POST',
  body: formData
});
```

This enhanced API provides full multimedia support while maintaining backward compatibility with existing JSON-only submissions.
