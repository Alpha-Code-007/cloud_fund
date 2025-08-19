package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByOrderId(String orderId);
    Optional<Donation> findByPaymentId(String paymentId);

    // New method to calculate total successful donations for a cause
    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.cause.id = :causeId AND d.status = :status")
    Double sumDonationsByCauseAndStatus(@Param("causeId") Long causeId,
                                        @Param("status") Donation.DonationStatus status);
}
