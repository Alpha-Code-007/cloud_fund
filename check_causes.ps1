# Check Public Causes Endpoint
# This script checks the current public causes and their video upload status

$baseUrl = "http://localhost:8080"

Write-Host "Checking Public Causes Endpoint" -ForegroundColor Green

# Function to check if server is running
function Test-ServerStatus {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/public/causes" -Method GET -TimeoutSec 5
        return $true
    } catch {
        return $false
    }
}

# Function to display causes information
function Show-CausesInfo {
    try {
        Write-Host "`nFetching public causes..." -ForegroundColor Yellow
        $causes = Invoke-RestMethod -Uri "$baseUrl/api/public/causes" -Method GET
        
        if ($causes -and $causes.Count -gt 0) {
            Write-Host "`nFound $($causes.Count) public causes:" -ForegroundColor Green
            
            foreach ($cause in $causes) {
                Write-Host "`n=== Cause: $($cause.title) ===" -ForegroundColor Cyan
                Write-Host "ID: $($cause.id)" -ForegroundColor White
                Write-Host "Description: $($cause.description)" -ForegroundColor Gray
                Write-Host "Target Amount: $($cause.targetAmount)" -ForegroundColor White
                Write-Host "Current Amount: $($cause.currentAmount)" -ForegroundColor White
                Write-Host "Status: $($cause.status)" -ForegroundColor White
                Write-Host "Media Type: $($cause.mediaType)" -ForegroundColor Yellow
                
                if ($cause.imageUrl) {
                    Write-Host "Image URL: $($cause.imageUrl)" -ForegroundColor Green
                } else {
                    Write-Host "Image URL: Not set" -ForegroundColor Red
                }
                
                if ($cause.videoUrl) {
                    Write-Host "Video URL: $($cause.videoUrl)" -ForegroundColor Green
                } else {
                    Write-Host "Video URL: Not set" -ForegroundColor Red
                }
                
                Write-Host "Category: $($cause.category)" -ForegroundColor White
                Write-Host "Location: $($cause.location)" -ForegroundColor White
                Write-Host "Created At: $($cause.createdAt)" -ForegroundColor Gray
                Write-Host "----------------------------------------" -ForegroundColor Gray
            }
        } else {
            Write-Host "`nNo public causes found." -ForegroundColor Yellow
            Write-Host "This could mean:" -ForegroundColor Cyan
            Write-Host "1. No causes have been created yet" -ForegroundColor White
            Write-Host "2. No personal cause submissions have been approved" -ForegroundColor White
            Write-Host "3. Database is empty" -ForegroundColor White
        }
        
    } catch {
        Write-Host "`nError fetching causes: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Make sure the server is running on port 8080" -ForegroundColor Yellow
    }
}

# Function to show video upload capabilities
function Show-VideoUploadInfo {
    Write-Host "`n=== Video Upload Capabilities ===" -ForegroundColor Green
    Write-Host "The following endpoints now support video uploads:" -ForegroundColor White
    Write-Host "1. POST /api/personal-cause-submissions/with-image" -ForegroundColor Cyan
    Write-Host "   - Supports both 'image' and 'video' form parameters" -ForegroundColor Gray
    Write-Host "2. POST /api/causes/upload-video" -ForegroundColor Cyan
    Write-Host "   - Direct video upload for causes" -ForegroundColor Gray
    Write-Host "3. POST /api/events/upload-video" -ForegroundColor Cyan
    Write-Host "   - Direct video upload for events" -ForegroundColor Gray
    Write-Host "4. POST /api/upload-media" -ForegroundColor Cyan
    Write-Host "   - Generic media upload supporting both images and videos" -ForegroundColor Gray
    
    Write-Host "`nSupported video formats:" -ForegroundColor Yellow
    $videoFormats = @("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv", "m4v", "3gp", "ogv")
    Write-Host "$($videoFormats -join ', ')" -ForegroundColor White
    
    Write-Host "`nVideo file size limit: 100MB" -ForegroundColor Yellow
    Write-Host "Image file size limit: 10MB" -ForegroundColor Yellow
}

# Check server status
Write-Host "Checking if server is running..." -ForegroundColor Yellow
if (Test-ServerStatus) {
    Write-Host "✓ Server is running on port 8080" -ForegroundColor Green
    Show-CausesInfo
} else {
    Write-Host "✗ Server is not running or not responding" -ForegroundColor Red
    Write-Host "Please start the server with: mvn spring-boot:run" -ForegroundColor Yellow
}

Show-VideoUploadInfo

Write-Host "`n=== Testing Instructions ===" -ForegroundColor Green
Write-Host "To test video upload functionality:" -ForegroundColor White
Write-Host "1. Start the server: mvn spring-boot:run" -ForegroundColor Cyan
Write-Host "2. Use Postman or curl to test the endpoints" -ForegroundColor Cyan
Write-Host "3. Submit a personal cause with video using:" -ForegroundColor Cyan
Write-Host "   POST /api/personal-cause-submissions/with-image" -ForegroundColor Gray
Write-Host "4. Admin can approve the submission to create a public cause" -ForegroundColor Cyan
Write-Host "5. Check this endpoint again to see the cause with video" -ForegroundColor Cyan

Write-Host "`nDone!" -ForegroundColor Green
