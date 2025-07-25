package com.donorbox.backend.controller;

import com.donorbox.backend.entity.*;
import com.donorbox.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "Admin endpoints for managing content")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private final CauseService causeService;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final ImageUploadService imageUploadService;

    // Admin Causes Management
    @GetMapping("/causes")
    @Operation(summary = "Admin - Get all causes", description = "Retrieve all causes for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved causes", 
                 content = @Content(mediaType = "application/json", 
                                  array = @ArraySchema(schema = @Schema(implementation = Cause.class))))
    public ResponseEntity<List<Cause>> getAllCauses() {
        List<Cause> causes = causeService.getAllCauses();
        return ResponseEntity.ok(causes);
    }

    @GetMapping("/causes/{id}")
    @Operation(summary = "Admin - Get cause by ID", description = "Retrieve specific cause for admin")
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

@PostMapping("/causes")
@Operation(summary = "Admin - Create cause", description = "Create a new cause")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cause created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
})
public ResponseEntity<Cause> createCause(@Valid @RequestBody Cause cause) {
    if (cause.getTitle() == null || cause.getDescription() == null || cause.getTargetAmount() == null) {
        return ResponseEntity.badRequest().build();
    }
    Cause createdCause = causeService.createCause(cause);
    return new ResponseEntity<>(createdCause, HttpStatus.CREATED);
}

    @PutMapping("/causes/{id}")
    @Operation(summary = "Admin - Update cause", description = "Update an existing cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cause updated successfully",
                        content = @Content(mediaType = "application/json", 
                                         schema = @Schema(implementation = Cause.class))),
            @ApiResponse(responseCode = "404", description = "Cause not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Cause> updateCause(
            @Parameter(description = "ID of the cause to update")
            @PathVariable Long id,
            @Valid @RequestBody Cause cause) {
        Cause updatedCause = causeService.updateCause(id, cause);
        return ResponseEntity.ok(updatedCause);
    }

    @DeleteMapping("/causes/{id}")
    @Operation(summary = "Admin - Delete cause", description = "Delete a cause")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cause deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Cause not found")
    })
    public ResponseEntity<Void> deleteCause(
            @Parameter(description = "ID of the cause to delete")
            @PathVariable Long id) {
        causeService.deleteCause(id);
        return ResponseEntity.noContent().build();
    }

    // Admin Events Management
    @GetMapping("/events")
    @Operation(summary = "Admin - Get all events", description = "Retrieve all events for admin management")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Admin - Get event by ID", description = "Retrieve specific event for admin")
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

    @PostMapping("/events")
    @Operation(summary = "Admin - Create event", description = "Create a new event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @PutMapping("/events/{id}")
    @Operation(summary = "Admin - Update event", description = "Update an existing event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> updateEvent(
            @Parameter(description = "ID of the event to update")
            @PathVariable Long id,
            @Valid @RequestBody Event event) {
        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{id}")
    @Operation(summary = "Admin - Delete event", description = "Delete an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete")
            @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


    // ===== RESTORED MULTIPART FORM ENDPOINTS FOR IMAGE UPLOADS =====

    /**
     * Create cause with image upload (multipart form)
     */
    @PostMapping(value = "/causes/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create cause with image", description = "Create a new cause with image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cause created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<Cause> createCauseWithImage(
            @Parameter(description = "Cause title") @RequestParam("title") String title,
            @Parameter(description = "Cause description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam("targetAmount") String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Create cause object
            Cause cause = Cause.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .targetAmount(new java.math.BigDecimal(targetAmount))
                    .category(category)
                    .location(location)
                    .build();
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = imageUploadService.uploadImage(image, "causes");
                cause.setImageUrl(imagePath); // Store relative path, not full URL
            }
            
            Cause createdCause = causeService.createCause(cause);
            return new ResponseEntity<>(createdCause, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update cause with image upload (multipart form)
     */
    @PutMapping(value = "/causes/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update cause with image", description = "Update an existing cause with optional image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cause updated successfully"),
            @ApiResponse(responseCode = "404", description = "Cause not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Cause> updateCauseWithImage(
            @Parameter(description = "ID of the cause to update") @PathVariable Long id,
            @Parameter(description = "Cause title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Cause description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Target amount") @RequestParam(value = "targetAmount", required = false) String targetAmount,
            @Parameter(description = "Category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Get existing cause
            Cause existingCause = causeService.getCauseById(id);
            
            // Update fields if provided
            if (title != null) existingCause.setTitle(title);
            if (description != null) existingCause.setDescription(description);
            if (shortDescription != null) existingCause.setShortDescription(shortDescription);
            if (targetAmount != null) existingCause.setTargetAmount(new java.math.BigDecimal(targetAmount));
            if (category != null) existingCause.setCategory(category);
            if (location != null) existingCause.setLocation(location);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existingCause.getImageUrl() != null) {
                    imageUploadService.deleteImage(existingCause.getImageUrl());
                }
                
                String imagePath = imageUploadService.uploadImage(image, "causes");
                existingCause.setImageUrl(imagePath);
            }
            
            Cause updatedCause = causeService.updateCause(id, existingCause);
            return ResponseEntity.ok(updatedCause);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create event with image upload (multipart form)
     */
    @PostMapping(value = "/events/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Create event with image", description = "Create a new event with image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully with image"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or image upload failed")
    })
    public ResponseEntity<Event> createEventWithImage(
            @Parameter(description = "Event title") @RequestParam("title") String title,
            @Parameter(description = "Event description") @RequestParam("description") String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Event date (ISO format)") @RequestParam("eventDate") String eventDate,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Max participants") @RequestParam(value = "maxParticipants", required = false) String maxParticipants,
            @Parameter(description = "Current participants") @RequestParam(value = "currentParticipants", required = false) String currentParticipants,
            @Parameter(description = "Image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Create event object
            Event event = Event.builder()
                    .title(title)
                    .description(description)
                    .shortDescription(shortDescription)
                    .eventDate(java.time.LocalDateTime.parse(eventDate))
                    .location(location)
                    .build();
            
            if (maxParticipants != null) {
                event.setMaxParticipants(Integer.parseInt(maxParticipants));
            }
            
            // Set current participants (default to 0 if not provided)
            if (currentParticipants != null && !currentParticipants.trim().isEmpty()) {
                event.setCurrentParticipants(Integer.parseInt(currentParticipants));
            } else {
                event.setCurrentParticipants(0);
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = imageUploadService.uploadImage(image, "events");
                event.setImageUrl(imagePath); // Store relative path, not full URL
            }
            
            Event createdEvent = eventService.createEvent(event);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update event with image upload (multipart form)
     */
    @PutMapping(value = "/events/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin - Update event with image", description = "Update an existing event with optional image upload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Event> updateEventWithImage(
            @Parameter(description = "ID of the event to update") @PathVariable Long id,
            @Parameter(description = "Event title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Event description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Short description") @RequestParam(value = "shortDescription", required = false) String shortDescription,
            @Parameter(description = "Event date (ISO format)") @RequestParam(value = "eventDate", required = false) String eventDate,
            @Parameter(description = "Location") @RequestParam(value = "location", required = false) String location,
            @Parameter(description = "Max participants") @RequestParam(value = "maxParticipants", required = false) String maxParticipants,
            @Parameter(description = "Image file") @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Get existing event
            Event existingEvent = eventService.getEventById(id);
            
            // Update fields if provided
            if (title != null) existingEvent.setTitle(title);
            if (description != null) existingEvent.setDescription(description);
            if (shortDescription != null) existingEvent.setShortDescription(shortDescription);
            if (eventDate != null) existingEvent.setEventDate(java.time.LocalDateTime.parse(eventDate));
            if (location != null) existingEvent.setLocation(location);
            if (maxParticipants != null) existingEvent.setMaxParticipants(Integer.parseInt(maxParticipants));
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                // Delete old image if exists
                if (existingEvent.getImageUrl() != null) {
                    imageUploadService.deleteImage(existingEvent.getImageUrl());
                }
                
                String imagePath = imageUploadService.uploadImage(image, "events");
                existingEvent.setImageUrl(imagePath);
            }
            
            Event updatedEvent = eventService.updateEvent(id, existingEvent);
            return ResponseEntity.ok(updatedEvent);
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin Volunteers Management
    @GetMapping("/volunteers")
    @Operation(summary = "Admin - Get all volunteers", description = "Retrieve all volunteer registrations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved volunteers")
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }
}
