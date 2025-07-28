# Cloud Fund API Endpoint Testing Script
# Tests all endpoints to verify they are working correctly

$baseUrl = "http://localhost:8080"
$adminAuth = @{
    'Authorization' = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
}

Write-Host "🚀 Starting Cloud Fund API Endpoint Testing..." -ForegroundColor Green
Write-Host "Base URL: $baseUrl" -ForegroundColor Yellow
Write-Host "="*60 -ForegroundColor Blue

# Helper function to test endpoint
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [string]$Description
    )
    
    try {
        Write-Host "Testing: $Method $Url" -ForegroundColor Cyan
        Write-Host "Description: $Description" -ForegroundColor Gray
        
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            TimeoutSec = 10
        }
        
        if ($Body) {
            if ($Body -is [string]) {
                $params['Body'] = $Body
                $params['ContentType'] = 'application/json'
            } else {
                $params['Body'] = ($Body | ConvertTo-Json -Depth 3)
                $params['ContentType'] = 'application/json'
            }
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "✅ SUCCESS - Status: OK" -ForegroundColor Green
        
        # Show sample response for important endpoints
        if ($Method -eq "GET" -and $response -is [array] -and $response.Count -gt 0) {
            Write-Host "   Sample data: Found $($response.Count) items" -ForegroundColor DarkGreen
        } elseif ($response -and $response.GetType().Name -ne "String") {
            $sampleProps = $response | Get-Member -MemberType NoteProperty | Select-Object -First 3 -ExpandProperty Name
            if ($sampleProps) {
                Write-Host "   Sample properties: $($sampleProps -join ', ')" -ForegroundColor DarkGreen
            }
        }
        
        return $true
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $statusText = $_.Exception.Response.StatusCode
        
        if ($statusCode -eq 401) {
            Write-Host "🔐 UNAUTHORIZED - Requires authentication" -ForegroundColor Yellow
        } elseif ($statusCode -eq 404) {
            Write-Host "❌ NOT FOUND - Endpoint may not exist or resource not found" -ForegroundColor Red
        } elseif ($statusCode -eq 400) {
            Write-Host "⚠️  BAD REQUEST - Invalid request data" -ForegroundColor Orange
        } elseif ($statusCode -eq 500) {
            Write-Host "🔥 SERVER ERROR - Internal server error" -ForegroundColor Red
        } else {
            Write-Host "❌ FAILED - Status: $statusCode $statusText" -ForegroundColor Red
        }
        
        if ($_.Exception.Message) {
            Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor DarkRed
        }
        
        return $false
    }
    finally {
        Write-Host ""
    }
}

# Test Results Tracking
$testResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    RequiresAuth = 0
    NotFound = 0
}

function Update-TestResult {
    param([bool]$Success, [int]$StatusCode = 0)
    $testResults.Total++
    if ($Success) {
        $testResults.Passed++
    } else {
        $testResults.Failed++
        if ($StatusCode -eq 401) {
            $testResults.RequiresAuth++
        } elseif ($StatusCode -eq 404) {
            $testResults.NotFound++
        }
    }
}

# Start Testing
Write-Host "🧪 BASIC HEALTH CHECKS" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Health Check
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/health" -Description "Application health check"
Update-TestResult -Success $result

# Swagger Redirect
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/" -Description "Root redirect to Swagger"
Update-TestResult -Success $result

Write-Host "🔐 PUBLIC API ENDPOINTS" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Public Causes
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/causes" -Description "Get all public causes"
Update-TestResult -Success $result

# Public Events
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/events" -Description "Get all public events"
Update-TestResult -Success $result

# Public Donations
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/donations" -Description "Get all donations"
Update-TestResult -Success $result

# Homepage Stats
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/homepage-stats" -Description "Get homepage statistics"
Update-TestResult -Success $result

# Payment Currencies
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/payment/currencies" -Description "Get supported payment currencies"
Update-TestResult -Success $result

# Public Blogs
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/blogs" -Description "Get published blogs"
Update-TestResult -Success $result

# Featured Blogs
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/blogs/featured" -Description "Get featured blogs"
Update-TestResult -Success $result

Write-Host "📝 PUBLIC POST ENDPOINTS (Sample Data)" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Test Volunteer Registration
$volunteerData = @{
    name = "Test Volunteer"
    email = "test@example.com"
    phone = "1234567890"
    skills = "Testing, Documentation"
    availability = "Weekends"
    message = "I want to help with testing"
}

$result = Test-Endpoint -Method "POST" -Url "$baseUrl/api/public/volunteer/register" -Body $volunteerData -Description "Register as volunteer"
Update-TestResult -Success $result

# Test Contact Form
$contactData = @{
    name = "Test User"
    email = "test@example.com"
    subject = "Test Message"
    message = "This is a test contact message"
}

$result = Test-Endpoint -Method "POST" -Url "$baseUrl/api/public/contact/send" -Body $contactData -Description "Send contact message"
Update-TestResult -Success $result

# Test Donation (without payment processing)
$donationData = @{
    amount = 100.00
    currency = "USD"
    donorName = "Test Donor"
    donorEmail = "donor@example.com"
    donorPhone = "1234567890"
    message = "Test donation"
    isAnonymous = $false
}

$result = Test-Endpoint -Method "POST" -Url "$baseUrl/api/public/donate" -Body $donationData -Description "Make a test donation"
Update-TestResult -Success $result

Write-Host "🔒 ADMIN ENDPOINTS (Protected)" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Admin Causes
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/admin/causes" -Headers $adminAuth -Description "Get all causes (Admin)"
Update-TestResult -Success $result

# Admin Events
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/admin/events" -Headers $adminAuth -Description "Get all events (Admin)"
Update-TestResult -Success $result

# Admin Volunteers
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/admin/volunteers" -Headers $adminAuth -Description "Get all volunteers (Admin)"
Update-TestResult -Success $result

# Admin Blogs
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/admin/blogs" -Headers $adminAuth -Description "Get all blogs (Admin)"
Update-TestResult -Success $result

# Admin Blogs Paginated
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/admin/blogs/paginated?page=0&size=5" -Headers $adminAuth -Description "Get paginated blogs (Admin)"
Update-TestResult -Success $result

Write-Host "🖼️ IMAGE API ENDPOINTS" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Note: Image upload endpoints require multipart/form-data which is complex to test with simple REST calls
# Testing the GET endpoint for images (will likely return 404 if no images exist)
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/images/causes/test.jpg" -Description "Get sample image (may not exist)"
Update-TestResult -Success $result

Write-Host "📊 ADVANCED PUBLIC ENDPOINTS" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Test Payment Order Creation (will likely fail without proper Razorpay configuration)
try {
    $paymentData = "amount=100&currency=USD&receiptId=TEST_123"
    $headers = @{
        'Content-Type' = 'application/x-www-form-urlencoded'
    }
    $result = Test-Endpoint -Method "POST" -Url "$baseUrl/api/public/payment/create-order" -Body $paymentData -Headers $headers -Description "Create payment order"
    Update-TestResult -Success $result
} catch {
    Write-Host "Payment endpoint may require valid Razorpay credentials" -ForegroundColor Yellow
    Update-TestResult -Success $false
}

Write-Host "🔍 INDIVIDUAL RESOURCE ENDPOINTS" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Test individual resource endpoints (may return 404 if no data exists)
$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/causes/1" -Description "Get specific cause by ID"
Update-TestResult -Success $result

$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/events/1" -Description "Get specific event by ID"
Update-TestResult -Success $result

$result = Test-Endpoint -Method "GET" -Url "$baseUrl/api/public/blogs/test-blog" -Description "Get blog by slug (may not exist)"
Update-TestResult -Success $result

Write-Host "📈 BLOG UTILITY ENDPOINTS (Admin)" -ForegroundColor Magenta
Write-Host "-"*30 -ForegroundColor Blue

# Test slug generation
$slugData = @{
    title = "Test Blog Title for Slug Generation"
}

$result = Test-Endpoint -Method "POST" -Url "$baseUrl/admin/blogs/generate-slug" -Body $slugData -Headers $adminAuth -Description "Generate blog slug from title"
Update-TestResult -Success $result

# Test reading time calculation
$readingTimeData = @{
    content = "This is a sample blog content for testing reading time calculation. It contains multiple sentences to simulate a real blog post. The system should calculate an estimated reading time based on the word count and average reading speed."
}

$result = Test-Endpoint -Method "POST" -Url "$baseUrl/admin/blogs/calculate-reading-time" -Body $readingTimeData -Headers $adminAuth -Description "Calculate blog reading time"
Update-TestResult -Success $result

Write-Host "="*60 -ForegroundColor Blue
Write-Host "📊 TEST SUMMARY" -ForegroundColor Green
Write-Host "="*60 -ForegroundColor Blue

Write-Host "Total Endpoints Tested: $($testResults.Total)" -ForegroundColor White
Write-Host "✅ Passed: $($testResults.Passed)" -ForegroundColor Green
Write-Host "❌ Failed: $($testResults.Failed)" -ForegroundColor Red
Write-Host "🔐 Requires Auth: $($testResults.RequiresAuth)" -ForegroundColor Yellow
Write-Host "❓ Not Found: $($testResults.NotFound)" -ForegroundColor Orange

$successRate = [math]::Round(($testResults.Passed / $testResults.Total) * 100, 2)
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -gt 80) { "Green" } elseif ($successRate -gt 60) { "Yellow" } else { "Red" })

Write-Host ""
Write-Host "🔍 NOTES:" -ForegroundColor Cyan
Write-Host "• Some endpoints may require database records to return data" -ForegroundColor Gray
Write-Host "• Image upload endpoints require multipart/form-data testing" -ForegroundColor Gray
Write-Host "• Payment endpoints may require valid Razorpay configuration" -ForegroundColor Gray
Write-Host "• Admin endpoints require proper authentication" -ForegroundColor Gray
Write-Host "• 404 errors may indicate empty database or missing test data" -ForegroundColor Gray

Write-Host ""
Write-Host "🎯 To create test data, use the admin endpoints or Swagger UI at:" -ForegroundColor Yellow
Write-Host "$baseUrl/swagger-ui/index.html" -ForegroundColor Blue

Write-Host ""
Write-Host "✨ Testing Complete!" -ForegroundColor Green
