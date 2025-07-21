package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    // Additional query methods if needed
}
