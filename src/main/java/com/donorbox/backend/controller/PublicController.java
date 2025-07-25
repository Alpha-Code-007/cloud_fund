package com.donorbox.backend.controller;
 
import com.donorbox.backend.dto.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.util.List;
 
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public API", description = "Public endpoints for frontend")
public class PublicController {
 
    private final DonationService donationService;
    private final CauseService causeService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final ContactService contactService;
    private final StatsService statsService;
    private final PaymentService paymentService;
    private final EmailService emailService;
 
    // Donation Endpoints
    @PostMapping("/donate")
    @Operation(summary = "Make a donation", description = "Create a new donation record")
@ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created successfully", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Donation.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Donation> makeDonation(@Valid @RequestBody DonationRequest request) {
        Donation donation = donationService.createDonation(request);
        return new ResponseEntity<>(donation, HttpStatus.CREATED);
    }
 
    @PostMapping("/donate-with-notifications")
    @Operation(summary = "Make a donation with email notifications",
               description = "Create a donation record and send confirmation emails to donor and organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created and notifications sent successfully",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Donation.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Donation> makeDonationWithNotifications(@Valid @RequestBody DonationRequest request) {
        try {
            // Create the donation
            Donation donation = donationService.createDonation(request);
           
            // Get cause name for email notifications
            String causeName = donation.getCause() != null ? donation.getCause().getTitle() : "General Donation";
           
            // Send donor confirmation email
            sendDonorNotificationEmail(donation, causeName);
           
            // Send organization notification email
            sendOrganizationNotificationEmail(donation, causeName);
           
            log.info("Donation created with ID: {} and notifications sent", donation.getId());
           
            return new ResponseEntity<>(donation, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating donation with notifications", e);
            throw new RuntimeException("Failed to process donation with notifications: " + e.getMessage());
        }
    }
 
    @GetMapping("/donations")
    @Operation(summary = "Get all donations", description = "Retrieve all donation records")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved donations",
                 content = @Content(mediaType = "application/json",
                                  array = @ArraySchema(schema = @Schema(implementation = Donation.class))))
    public ResponseEntity<List<Donation>> getAllDonations() {
        List<Donation> donations = donationService.getAllDonations();
        return ResponseEntity.ok(donations);
    }
 
    // Causes Endpoints
    @GetMapping("/causes")
    @Operation(summary = "Get all causes", description = "Retrieve all active causes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved causes")
    public ResponseEntity<List<Cause>> getAllCauses() {
        List<Cause> causes = causeService.getAllCauses();
        return ResponseEntity.ok(causes);
    }
 
    @GetMapping("/causes/{id}")
    @Operation(summary = "Get cause by ID", description = "Retrieve detailed information about a specific cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cause",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = Cause.class))),
            @ApiResponse(responseCode = "404", description = "Cause not found")
    })
    public ResponseEntity<Cause> getCauseById(
            @Parameter(description = "ID of the cause to retrieve")
            @PathVariable Long id) {
        Cause cause = causeService.getCauseById(id);
        return ResponseEntity.ok(cause);
    }
 
    // Events Endpoints
    @GetMapping("/events")
    @Operation(summary = "Get all events", description = "Retrieve all events")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
 
    @GetMapping("/events/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieve detailed information about a specific event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "ID of the event to retrieve")
            @PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
 
    // Volunteer Endpoints
    @PostMapping("/volunteer/register")
    @Operation(summary = "Register as volunteer", description = "Register a new volunteer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Volunteer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Volunteer> registerVolunteer(@Valid @RequestBody VolunteerRequest request) {
        Volunteer volunteer = volunteerService.registerVolunteer(request);
        return new ResponseEntity<>(volunteer, HttpStatus.CREATED);
    }
 
    // Contact Endpoints
    @PostMapping("/contact/send")
    @Operation(summary = "Send contact message", description = "Submit a contact form message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Message> sendContactMessage(@Valid @RequestBody ContactRequest request) {
        Message message = contactService.sendMessage(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
 
    // Homepage Stats Endpoint
    @GetMapping("/homepage-stats")
    @Operation(summary = "Get homepage statistics", description = "Retrieve real-time statistics for the homepage")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    public ResponseEntity<HomepageStatsResponse> getHomepageStats() {
        HomepageStatsResponse stats = statsService.getHomepageStats();
        return ResponseEntity.ok(stats);
    }
   
    // Payment Endpoints
    @GetMapping("/payment/currencies")
    @Operation(summary = "Get supported currencies", description = "Retrieve list of supported currencies for international payments")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supported currencies")
    public ResponseEntity<java.util.Map<String, String>> getSupportedCurrencies() {
        java.util.Map<String, String> currencies = paymentService.getSupportedCurrencies();
        return ResponseEntity.ok(currencies);
    }
 
    @PostMapping("/payment/create-order")
    @Operation(summary = "Create payment order", description = "Create a Razorpay order for processing payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<java.util.Map<String, Object>> createPaymentOrder(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam String receiptId) {
        try {
            com.razorpay.Order order = paymentService.createOrder(amount, currency, receiptId);
           
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));
           
            // log.info("Payment order created successfully: {}", order.get("id"));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating payment order", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to create payment order: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
 
    @PostMapping("/donate-and-pay")
    @Operation(summary = "Create donation and payment order",
               description = "Create a donation record and corresponding Razorpay payment order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created and payment order generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<java.util.Map<String, Object>> createDonationAndPaymentOrder(@Valid @RequestBody DonationRequest request) {
        try {
            // Create the donation
            Donation donation = donationService.createDonation(request);
           
            // Generate a unique receipt ID using donation ID
            String receiptId = "DON_" + donation.getId() + "_" + System.currentTimeMillis();
           
            // Create Razorpay order
            com.razorpay.Order order = paymentService.createOrder(request.getAmount(), request.getCurrency(), receiptId);
           
            // Update donation with order ID
            donationService.updateDonationWithOrderId(donation.getId(), order.get("id").toString());
           
            // Prepare response
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("donationId", donation.getId());
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));
            response.put("donorName", donation.getDonorName());
            response.put("donorEmail", donation.getDonorEmail());
            response.put("causeName", donation.getCause() != null ? donation.getCause().getTitle() : "General Donation");
           
            log.info("Donation created with ID: {} and payment order: {}", donation.getId(), order.get("id"));
           
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating donation and payment order", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to create donation and payment order: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
 
    @PostMapping("/payment/verify")
    @Operation(summary = "Verify payment", description = "Verify payment transaction and send notification emails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment verification completed",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment verification data")
    })
    public ResponseEntity<Boolean> verifyPayment(@Valid @RequestBody PaymentVerificationRequest request) {
        boolean isVerified = paymentService.verifyPaymentAndSendNotifications(
            request.getOrderId(),
            request.getPaymentId(),
            request.getSignature(),
            request.getDonorEmail(),
            request.getDonorName(),
            request.getAmount(),
            request.getCurrency(),
            request.getCauseName()
        );
       
        // Update donation status if payment verification is successful
        if (isVerified) {
            try {
                // Find donation by order ID and update status
                Donation donation = donationService.findByOrderId(request.getOrderId());
                if (donation != null) {
                    donationService.updateDonationStatus(
                        donation.getId(),
                        Donation.DonationStatus.COMPLETED,
                        request.getPaymentId(),
                        request.getOrderId()
                    );
                    log.info("Donation status updated to COMPLETED for order: {}", request.getOrderId());
                }
            } catch (Exception e) {
                log.error("Error updating donation status for order: {}", request.getOrderId(), e);
                // Don't fail payment verification due to status update issues
            }
        }
       
        return ResponseEntity.ok(isVerified);
    }
   
    // Helper methods for email notifications
    private void sendDonorNotificationEmail(Donation donation, String causeName) {
    try {
        String status = donation.getStatus() != null ? donation.getStatus().toString() : "UNKNOWN";
        String statusColor = switch (donation.getStatus()) {
            case COMPLETED -> "#28a745";   // Green
            case FAILED -> "#dc3545";      // Red
            case PENDING -> "#ffc107";     // Orange
            case REFUNDED -> "#17a2b8";    // Blue
            default -> "#6c757d";          // Gray
        };
        String statusMessage = switch (donation.getStatus()) {
            case COMPLETED -> "We are delighted to confirm that your donation has been successfully received.";
            case PENDING -> "Your donation is currently pending. We will notify you once the payment is confirmed.";
            case FAILED -> "Unfortunately, your donation could not be processed. Please try again or contact support.";
            case REFUNDED -> "Your donation has been refunded. For further details, please contact support.";
            default -> "Your donation status is currently unknown. Please contact support for more information.";
        };
 
        String subject = switch (donation.getStatus()) {
            case COMPLETED -> "Your Donation is Complete - Thank You!";
            case PENDING -> "Your Donation is Pending - Action Required?";
            case FAILED -> "Action Required: Your Donation Failed";
            case REFUNDED -> "Your Donation Has Been Refunded";
            default -> "Update on Your Donation";
        };
 
        // --- Dynamic Main Heading based on Status ---
        String mainHeading = switch (donation.getStatus()) {
            case COMPLETED -> "Donation Confirmed - Thank You!";
            case PENDING -> "Your Donation is Pending";
            case FAILED -> "Donation Unsuccessful";
            case REFUNDED -> "Donation Refunded";
            default -> "Update on Your Donation";
        };
        // --- END
 
        String htmlContent = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "<h1 style='color: #2c5aa0; text-align: center;'>%s</h1>" + //  dynamic heading
            "<p>Dear %s,</p>" +
            "<p>%s</p>" +
            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "<h3 style='margin-top: 0; color: #2c5aa0;'>Donation Details:</h3>" +
            "<p><strong>Amount:</strong> %s %s</p>" +
            "<p><strong>Cause:</strong> %s</p>" +
            "<p><strong>Donation ID:</strong> %s</p>" +
            "<p><strong>Date:</strong> %s</p>" +
            "<p><strong>Phone:</strong> %s</p>" +
            "<p><strong>Status:</strong> <span style='color: %s;'>%s</span></p>" +
            "</div>" +
            "<p>Your generous contribution will make a real difference in supporting our cause. " +
            "We will keep you updated on how your donation is being used.</p>" +
            "<p>With heartfelt gratitude,<br>The DonorBox Team</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            mainHeading, // ---  dynamic 'mainHeading' variable here ---
            donation.getDonorName(),
            statusMessage,
            donation.getCurrency(),
            donation.getAmount(),
            causeName,
            donation.getId(),
            donation.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            donation.getDonorPhone() != null ? donation.getDonorPhone() : "Not provided",
            statusColor,
            status
        );
 
        emailService.sendHtmlEmail(donation.getDonorEmail(), subject, htmlContent);
 
    } catch (Exception e) {
        log.error("Error sending donor notification email for donation ID: {}", donation.getId(), e);
    }
}
   
    private void sendOrganizationNotificationEmail(Donation donation, String causeName) {
    try {
        String status = donation.getStatus() != null ? donation.getStatus().toString() : "UNKNOWN";
        String statusColor = switch (donation.getStatus()) {
            case COMPLETED -> "#28a745";   // Green
            case FAILED -> "#dc3545";      // Red
            case PENDING -> "#ffc107";     // Orange
            case REFUNDED -> "#17a2b8";    // Blue
            default -> "#6c757d";          // Gray
        };
 
        // --- NEW: Dynamic Subject Line for Organization ---
        String subject = switch (donation.getStatus()) {
            case COMPLETED -> "New Donation: " + donation.getCurrency() + " " + donation.getAmount() + " Received!";
            case PENDING -> "Pending Donation: " + donation.getCurrency() + " " + donation.getAmount() + " from " + donation.getDonorName();
            case FAILED -> "Failed Donation: " + donation.getCurrency() + " " + donation.getAmount() + " from " + donation.getDonorName();
            case REFUNDED -> "Donation Refunded: " + donation.getCurrency() + " " + donation.getAmount() + " (ID: " + donation.getId() + ")";
            default -> "Donation Status Update (ID: " + donation.getId() + ")";
        };
        // --- END NEW ---
 
        // --- NEW: Dynamic Main Heading for Organization ---
        String mainHeading = switch (donation.getStatus()) {
            case COMPLETED -> "New Donation Received!";
            case PENDING -> "New Pending Donation";
            case FAILED -> "Donation Attempt Failed";
            case REFUNDED -> "Donation Refunded";
            default -> "Donation Status Update";
        };
        // --- END NEW ---
 
        String htmlContent = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "<h1 style='color: #2c5aa0; text-align: center;'>%s</h1>" + // Changed from #28a745 to #2c5aa0 for consistency with donor email heading color, or keep green if preferred for "new"
            "<p>A new donation has been processed through the DonorBox platform with the following status:</p>" + // Adjusted introductory text
            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "<h3 style='margin-top: 0; color: #2c5aa0;'>Donation Details:</h3>" + // Also changed to #2c5aa0
            "<p><strong>Donor Name:</strong> %s</p>" +
            "<p><strong>Donor Email:</strong> %s</p>" +
            "<p><strong>Donor Phone:</strong> %s</p>" +
            "<p><strong>Amount:</strong> %s %s</p>" +
            "<p><strong>Cause:</strong> %s</p>" +
            "<p><strong>Donation ID:</strong> %s</p>" +
            "<p><strong>Date:</strong> %s</p>" +
            "<p><strong>Status:</strong> <span style='color: %s;'>%s</span></p>" +
            "<p><strong>Message:</strong> %s</p>" +
            "</div>" +
            "<p>Please log into the admin dashboard to view more details and manage this donation.</p>" +
            "<p>Best regards,<br>DonorBox System</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            mainHeading, // ---  Pass dynamic mainHeading ---
            donation.getDonorName() ,
            donation.getDonorEmail() ,
            donation.getDonorPhone() ,
            donation.getCurrency(),
            donation.getAmount(),
            causeName,
            donation.getId(),
            donation.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            statusColor,
            status,
            donation.getMessage() != null ? donation.getMessage() : "No message provided"
        );
 
        String adminEmail = "info.sairuraldevelopmenttrust@gmail.com";
        // ---Pass the dynamic 'subject' variable here ---
        emailService.sendHtmlEmail(adminEmail, subject, htmlContent);
        // --- END  ---
 
    } catch (Exception e) {
        log.error("Error sending organization notification email for donation ID: {}", donation.getId(), e);
    }}}