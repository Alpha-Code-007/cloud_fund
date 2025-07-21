package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.dto.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final CauseRepository causeRepository;
    private final PaymentService paymentService;

    @Transactional
    public Donation createDonation(DonationRequest request) {
        Cause cause = null;
        if (request.getCauseId() != null) {
            cause = causeRepository.findById(request.getCauseId())
                    .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
        }
        
        // Validate currency support
        if (!paymentService.isCurrencySupported(request.getCurrency())) {
            throw new IllegalArgumentException("Currency not supported: " + request.getCurrency());
        }
        
        Donation donation = Donation.builder()
                .donorName(request.getDonorName())
                .donorEmail(request.getDonorEmail())
                .donorPhone(request.getDonorPhone())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .cause(cause)
                .message(request.getMessage())
                .build();

        return donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }
}
