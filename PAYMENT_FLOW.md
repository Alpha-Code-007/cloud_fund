# Payment Flow Documentation

## Overview

This document describes the complete payment flow implementation for the DonorBox platform, including Razorpay integration, email notifications, and donation management.

## Components

### 1. Configuration (application.properties)

The following configurations are properly set up:

```properties
# RAZORPAY CONFIGURATION
razorpay.key.id=rzp_test_a3kb1GXuvUpqcu
razorpay.key.secret=Xor0KhdlApRxLBS9EZLSgj1p

# EMAIL CONFIGURATION
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=info.sairuraldevelopmenttrust@gmail.com
spring.mail.password=vyrjuzftuavkshyv
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ADMIN EMAIL
admin.email=info.sairuraldevelopmenttrust@gmail.com
```

### 2. Payment Endpoints

#### A. Get Supported Currencies
- **URL**: `GET /payment/currencies`
- **Description**: Retrieves list of supported currencies
- **Response**: Map of currency codes and names

#### B. Create Payment Order
- **URL**: `POST /payment/create-order`
- **Description**: Creates a Razorpay order for payment processing
- **Parameters**:
  - `amount` (BigDecimal): Payment amount
  - `currency` (String): Currency code (default: USD)
  - `receiptId` (String): Unique receipt identifier
- **Response**: Order details including orderId, amount, currency, etc.

#### C. Create Donation and Payment Order
- **URL**: `POST /donate-and-pay`
- **Description**: Creates both donation record and payment order in one step
- **Request Body**: DonationRequest JSON
- **Response**: Combined donation and order information

#### D. Verify Payment
- **URL**: `POST /payment/verify`
- **Description**: Verifies payment signature and sends notification emails
- **Request Body**: PaymentVerificationRequest JSON
- **Response**: Boolean indicating verification success

### 3. Payment Flow Steps

#### Step 1: Create Donation and Payment Order
```
POST /donate-and-pay
{
  "donorName": "John Doe",
  "donorEmail": "john@example.com",
  "donorPhone": "+1234567890",
  "amount": 100.00,
  "currency": "USD",
  "causeId": 1,
  "paymentMethod": "razorpay",
  "message": "Happy to contribute!"
}
```

#### Step 2: Process Payment (Frontend Integration)
Use the returned `orderId` and other details to initialize Razorpay payment on frontend.

#### Step 3: Verify Payment
```
POST /payment/verify
{
  "orderId": "order_xxxxx",
  "paymentId": "pay_xxxxx",
  "signature": "signature_xxxxx",
  "donorName": "John Doe",
  "donorEmail": "john@example.com",
  "amount": 100.00,
  "currency": "USD",
  "causeName": "Education for All"
}
```

### 4. Email Notifications

#### Donor Confirmation Email
- **Trigger**: After successful payment verification
- **Recipient**: Donor email address
- **Content**: 
  - Thank you message
  - Donation details (amount, cause, date, ID)
  - Contact information

#### Admin Notification Email
- **Trigger**: After successful payment verification
- **Recipient**: Admin email (info.sairuraldevelopmenttrust@gmail.com)
- **Content**:
  - New donation alert
  - Complete donor and donation details
  - Admin dashboard link suggestion

### 5. Database Updates

#### Donation Status Updates
- **Initial**: PENDING (when donation is created)
- **Success**: COMPLETED (after payment verification)
- **Failed**: FAILED (if payment fails)
- **Refunded**: REFUNDED (if refund is processed)

#### Payment Tracking
- Order ID and Payment ID are stored with donation record
- Enables complete audit trail

### 6. Error Handling

- Payment verification failures don't crash the system
- Email sending failures are logged but don't affect payment processing
- Database update failures are handled gracefully
- Comprehensive logging for debugging

### 7. Security Features

- Payment signature verification using Razorpay secret key
- Input validation on all endpoints
- Secure credential management
- Transaction integrity checks

### 8. Supported Currencies

The system supports international payments with currencies:
- INR (Indian Rupee)
- USD (US Dollar)
- EUR (Euro)
- GBP (British Pound)
- AUD (Australian Dollar)
- CAD (Canadian Dollar)
- SGD (Singapore Dollar)
- AED (UAE Dirham)
- MYR (Malaysian Ringgit)

### 9. Testing

To test the payment flow:

1. Start the application
2. Use `/donate-and-pay` to create a donation and get order details
3. Simulate frontend payment processing
4. Use `/payment/verify` to complete the verification
5. Check email notifications and database status

### 10. Frontend Integration Notes

For proper frontend integration:

1. Use the order details from `/donate-and-pay` response
2. Initialize Razorpay with the orderId
3. Handle payment success/failure callbacks
4. Call `/payment/verify` with all required parameters
5. Display appropriate success/failure messages to users

## Status: ✅ FULLY IMPLEMENTED

All payment endpoints are properly implemented with:
- ✅ Razorpay credentials configured
- ✅ Email configuration working
- ✅ Payment order creation
- ✅ Payment verification with signature validation
- ✅ Email notifications to donor and admin
- ✅ Database status updates
- ✅ Error handling and logging
- ✅ International currency support
- ✅ Complete audit trail
