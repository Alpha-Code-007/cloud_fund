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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Public API", description = "Public endpoints for frontend")
public class PublicController {

    private final DonationService donationService;
    private final CauseService causeService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final ContactService contactService;
    private final StatsService statsService;
    private final PaymentService paymentService;

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
            request.getDonorName(),
            request.getDonorEmail(),
            request.getAmount(),
            request.getCurrency(),
request.getCauseName()
        );
        return ResponseEntity.ok(isVerified);
    }
}
