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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Admin Volunteers Management
    @GetMapping("/volunteers")
    @Operation(summary = "Admin - Get all volunteers", description = "Retrieve all volunteer registrations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved volunteers")
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        List<Volunteer> volunteers = volunteerService.getAllVolunteers();
        return ResponseEntity.ok(volunteers);
    }
}
