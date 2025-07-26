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
    private final EmailService emailService;

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
        List<Donation> donations = donationRepository.findAll();
        System.out.println("Donations retrieved: " + donations.size());
        return donations;
    }

    @Transactional
    public Donation updateDonationStatus(Long donationId, Donation.DonationStatus status, String paymentId, String orderId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        donation.setStatus(status);
        if (paymentId != null) {
            donation.setPaymentId(paymentId);
        }
        if (orderId != null) {
            donation.setOrderId(orderId);
        }

        Donation updatedDonation = donationRepository.save(donation);

        // ✅ Use centralized EmailService
        emailService.sendDonationEmails(updatedDonation, "info.sairuraldevelopmenttrust@gmail.com");

        return updatedDonation;
    }

    @Transactional(readOnly = true)
    public Donation findByOrderId(String orderId) {
        return donationRepository.findByOrderId(orderId)
                .orElse(null);
    }

    @Transactional
    public Donation updateDonationWithOrderId(Long donationId, String orderId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        donation.setOrderId(orderId);
        return donationRepository.save(donation);
    }
}
