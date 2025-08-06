# `/submit` Endpoint - Simplified 2-Input Structure

## ✅ **Updated Structure**

The `/submit` endpoint now has a **simplified 2-input structure** for file uploads:

### **File Inputs (Only 2):**
1. **`media`** - Single field for either image OR video (auto-detection)
2. **`proofDocument`** - For documents (PDF, DOC, etc.)

## **API Endpoint**
```
POST http://localhost:8080/api/personal-cause-submissions/submit
Content-Type: multipart/form-data
```

## **Parameters**

### **Text Fields:**
- `title` (required)
- `description` (required) 
- `targetAmount` (required)
- `submitterName` (required)
- `submitterEmail` (required)
- `shortDescription` (optional)
- `category` (optional)
- `location` (optional)
- `endDate` (optional - ISO format)
- `submitterPhone` (optional)
- `submitterMessage` (optional)

### **File Fields (Only 2):**
- **`media`** (optional) - Image OR video file (auto-detected)
  - **Image formats**: JPG, PNG, GIF, WEBP, BMP, TIFF, SVG (max 10MB)
  - **Video formats**: MP4, AVI, MOV, WEBM, MKV, M4V, 3GP, OGV (max 100MB)
- **`proofDocument`** (optional) - Proof documents
  - **Document formats**: PDF, DOC, DOCX, JPG, PNG, etc.

## **Frontend Implementation**

### **HTML Form:**
```html
<form action="/api/personal-cause-submissions/submit" method="POST" enctype="multipart/form-data">
    <!-- Text fields -->
    <input type="text" name="title" required />
    <textarea name="description" required></textarea>
    <input type="number" name="targetAmount" required />
    <input type="text" name="submitterName" required />
    <input type="email" name="submitterEmail" required />
    
    <!-- Only 2 file inputs -->
    <input type="file" name="media" accept="image/*,video/*" />
    <input type="file" name="proofDocument" accept=".pdf,.doc,.docx" />
    
    <button type="submit">Submit</button>
</form>
```

### **JavaScript/Fetch API:**
```javascript
const formData = new FormData();

// Text data
formData.append('title', 'Medical Emergency Fund');
formData.append('description', 'Need urgent medical help');
formData.append('targetAmount', '25000');
formData.append('submitterName', 'John Doe');
formData.append('submitterEmail', 'john@example.com');

// Only 2 file fields
formData.append('media', mediaFile);        // Can be image OR video
formData.append('proofDocument', docFile);  // PDF, DOC, etc.

fetch('/api/personal-cause-submissions/submit', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => console.log('Success:', data));
```

### **React Example:**
```jsx
const [mediaFile, setMediaFile] = useState(null);
const [proofFile, setProofFile] = useState(null);

const handleSubmit = async (formData) => {
  const data = new FormData();
  
  // Add text fields
  Object.keys(formData).forEach(key => {
    data.append(key, formData[key]);
  });
  
  // Add only 2 file fields
  if (mediaFile) data.append('media', mediaFile);
  if (proofFile) data.append('proofDocument', proofFile);
  
  const response = await fetch('/api/personal-cause-submissions/submit', {
    method: 'POST',
    body: data
  });
  
  return response.json();
};

// JSX
<input 
  type="file" 
  accept="image/*,video/*" 
  onChange={(e) => setMediaFile(e.target.files[0])} 
/>
<input 
  type="file" 
  accept=".pdf,.doc,.docx" 
  onChange={(e) => setProofFile(e.target.files[0])} 
/>
```

## **cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/personal-cause-submissions/submit" \
  -F "title=Help Medical Emergency" \
  -F "description=Need urgent medical treatment" \
  -F "targetAmount=25000" \
  -F "submitterName=John Doe" \
  -F "submitterEmail=john@example.com" \
  -F "media=@hospital_video.mp4" \
  -F "proofDocument=@medical_certificate.pdf"
```

## **Auto-Detection Logic:**

The API automatically determines if the `media` file is an image or video:

```java
// Backend logic (handled automatically)
if (mediaUploadService.isImageFile(media.getOriginalFilename())) {
    // Saves as imageUrl in database
} else if (mediaUploadService.isVideoFile(media.getOriginalFilename())) {
    // Saves as videoUrl in database
}
```

## **Response:**
```json
{
  "id": 123,
  "title": "Help Medical Emergency",
  "description": "Need urgent medical treatment",
  "targetAmount": 25000.00,
  "imageUrl": null,
  "videoUrl": "http://localhost:8080/api/media/personal-causes/hospital_video.mp4",
  "proofDocumentUrl": "http://localhost:8080/api/documents/proof-documents/medical_certificate.pdf",
  "proofDocumentName": "medical_certificate.pdf",
  "proofDocumentType": "pdf",
  "submitterName": "John Doe",
  "submitterEmail": "john@example.com",
  "status": "PENDING",
  "createdAt": "2024-12-03T16:45:00",
  "updatedAt": "2024-12-03T16:45:00"
}
```

## **Benefits:**
✅ **Simplified** - Only 2 file inputs instead of 3  
✅ **Flexible** - Single `media` field accepts both images and videos  
✅ **Auto-detection** - No need to specify file type  
✅ **User-friendly** - Easier for frontend implementation  
✅ **Backward compatible** - Other endpoints still available  

This gives you exactly what you requested: **2 file inputs only** with automatic image/video detection! 🎉
