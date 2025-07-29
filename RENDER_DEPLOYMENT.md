# Deploying to Render with Docker

This guide will help you deploy your Spring Boot application to Render using Docker.

## Prerequisites

1. A GitHub repository with your code
2. A Render account (free tier available)
3. A MySQL database (you can use Render's managed database or external service)

## Step 1: Push Your Code to GitHub

Make sure all the files are committed and pushed to your GitHub repository:

```bash
git add .
git commit -m "Add Docker configuration for Render deployment"
git push origin main
```

## Step 2: Set Up Database on Render

1. Go to your Render dashboard
2. Click "New" → "PostgreSQL" (or use external MySQL)
3. Name your database (e.g., "donorbox-db")
4. Choose your region and plan
5. Click "Create Database"
6. Note down the connection details

## Step 3: Create Web Service on Render

1. Go to your Render dashboard
2. Click "New" → "Web Service"
3. Connect your GitHub repository
4. Configure the service:
   - **Name**: `donorbox-backend`
   - **Environment**: `Docker`
   - **Region**: Choose closest to your users
   - **Branch**: `main`
   - **Root Directory**: Leave empty (or specify if different)

## Step 4: Set Environment Variables

In the Render web service settings, add these environment variables:

### Required Environment Variables:

```
SPRING_PROFILES_ACTIVE=production
PORT=8080
```

### Database Configuration:
```
DATABASE_URL=jdbc:mysql://your-db-host:3306/your-db-name?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password
```

### Security Configuration:
```
JWT_SECRET=your-super-long-and-secure-jwt-secret-key-here
JWT_EXPIRATION=86400
```

### Email Configuration - Alphaseam Mail Server:
```
MAIL_HOST=mail.alphaseam.com
MAIL_PORT=587
MAIL_USERNAME=testing@alphaseam.com
MAIL_PASSWORD=Alphaseam@!#8520
ADMIN_EMAIL=testing@alphaseam.com

# Optional IMAP/POP3 settings (for reference)
IMAP_HOST=mail.alphaseam.com
IMAP_PORT=993
POP3_HOST=mail.alphaseam.com
POP3_PORT=995
```

### Payment Configuration:
```
RAZORPAY_KEY_ID=your-razorpay-key-id
RAZORPAY_KEY_SECRET=your-razorpay-secret
```

## Step 5: Deploy

1. Click "Create Web Service"
2. Render will automatically build and deploy your application
3. The build process will:
   - Use the Dockerfile to build your image
   - Install dependencies with Maven
   - Create the JAR file
   - Start your Spring Boot application

## Step 6: Verify Deployment

Once deployed, you can access:
- **API**: `https://your-app-name.onrender.com`
- **Swagger UI**: `https://your-app-name.onrender.com/swagger-ui/index.html`
- **API Docs**: `https://your-app-name.onrender.com/v3/api-docs`

## Troubleshooting

### Common Issues:

1. **Database Connection Issues**:
   - Verify DATABASE_URL format
   - Check database credentials
   - Ensure database allows external connections

2. **Build Failures**:
   - Check build logs in Render dashboard
   - Verify Dockerfile syntax
   - Ensure all dependencies are in pom.xml

3. **Application Won't Start**:
   - Check application logs
   - Verify environment variables
   - Ensure PORT is set to 8080

### Health Check

Add this endpoint to monitor your application:

```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    Map<String, String> status = new HashMap<>();
    status.put("status", "UP");
    status.put("timestamp", Instant.now().toString());
    return ResponseEntity.ok(status);
}
```

### Database Migration

If you need to run database migrations, you can:
1. Use Hibernate's `ddl-auto=update` (already configured)
2. Or add Flyway for more controlled migrations

## Cost Considerations

- **Free Tier**: 750 hours/month, sleeps after 15 minutes of inactivity
- **Starter Plan**: $7/month, no sleeping, custom domains
- **Standard Plan**: $25/month, more resources

## Security Best Practices

1. **Never commit secrets** to your repository
2. **Use strong JWT secrets** (at least 32 characters)
3. **Enable HTTPS** (Render provides this automatically)
4. **Use environment-specific configurations**
5. **Regularly update dependencies**

## Monitoring

- **Logs**: Available in Render dashboard
- **Metrics**: CPU, Memory, and Request metrics
- **Alerts**: Set up notifications for downtime

Your application should now be successfully deployed on Render!
