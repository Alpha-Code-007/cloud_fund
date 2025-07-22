# Charity Backend API Endpoints

## Admin Endpoints

### Create Cause
- **Method**: POST
- **Path**: `/admin/causes`
- **Request Body**:
  ```json
  {
    "title": "Save the Children",
    "description": "Helping children in need.",
    "targetAmount": 5000
  }
  ```
- **Expected Response**:
  - **201 Created**: Cause created successfully.
  - **400 Bad Request**: Invalid request data.

### Get All Causes
- **Method**: GET
- **Path**: `/admin/causes`
- **Expected Response**:
  - **200 OK**: List of causes.

## Public Endpoints

### Make a Donation
- **Method**: POST
- **Path**: `/donate`
- **Request Body**:
  ```json
  {
    "amount": 100,
    "causeId": 1,
    "donor": "John Doe"
  }
  ```
- **Expected Response**:
  - **201 Created**: Donation made successfully.

### Get All Events
- **Method**: GET
- **Path**: `/events`
- **Expected Response**:
  - **200 OK**: List of events.

## Health Check

### Health
- **Method**: GET
- **Path**: `/health`
- **Expected Response**:
  - **200 OK**: "Donorbox Backend API is running!"

