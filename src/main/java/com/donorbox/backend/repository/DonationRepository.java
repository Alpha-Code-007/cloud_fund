package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByOrderId(String orderId);
    Optional<Donation> findByPaymentId(String paymentId);
}
