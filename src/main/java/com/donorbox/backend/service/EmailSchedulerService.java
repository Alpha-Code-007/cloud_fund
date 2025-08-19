package com.donorbox.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.donorbox.backend.entity.Donation;
import com.donorbox.backend.repository.DonationRepository;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailSchedulerService {

    private final TaskScheduler taskScheduler;
    private final DonationRepository donationRepository;
    private final EmailService emailService;

    public void scheduleDonationEmail(Long donationId, String orgEmail) {
        donationRepository.findById(donationId).ifPresent(donation -> {
            String status = donation.getStatus().name();

            if ("SUCCESS".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
                // Send immediately
                emailService.sendDonationEmails(donation, orgEmail);
            } else {
                // Schedule for 10 minutes later (likely pending case)
                Instant sendTime = Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(10));

                taskScheduler.schedule(() -> {
                    donationRepository.findById(donationId).ifPresent(latestDonation -> {
                        emailService.sendDonationEmails(latestDonation, orgEmail);
                    });
                }, sendTime);
            }
        });
    }
}
