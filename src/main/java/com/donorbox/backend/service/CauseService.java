package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CauseService {
    private final CauseRepository causeRepository;
    private final ImageUploadService imageUploadService;
    @Transactional(readOnly = true)
public List<Cause> getAllCauses() {
    List<Cause> causes = causeRepository.findAll();
    System.out.println("Causes retrieved: " + causes.size());
        return causeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cause getCauseById(Long id) {
        return causeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
    }

    @Transactional
    public Cause createCause(Cause cause) {
        return causeRepository.save(cause);
    }

    @Transactional
    public Cause updateCause(Long id, Cause cause) { 
        Cause existingCause = getCauseById(id);
        existingCause.setTitle(cause.getTitle());
        existingCause.setDescription(cause.getDescription());
        existingCause.setShortDescription(cause.getShortDescription());
        existingCause.setTargetAmount(cause.getTargetAmount());
        existingCause.setImageUrl(cause.getImageUrl());
        existingCause.setCategory(cause.getCategory());
        existingCause.setLocation(cause.getLocation());
        existingCause.setEndDate(cause.getEndDate());
        existingCause.setStatus(cause.getStatus());
        return causeRepository.save(existingCause);
    }

    @Transactional
    public void deleteCause(Long id) {
        // Get the cause to check if it has an image
        Cause cause = getCauseById(id);
        
        // Delete associated image if exists
        if (cause.getImageUrl() != null && !cause.getImageUrl().trim().isEmpty()) {
            imageUploadService.deleteImage(cause.getImageUrl());
        }
        
        // Delete the cause from database
        causeRepository.deleteById(id);
    }
}

