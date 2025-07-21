package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Cause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauseRepository extends JpaRepository<Cause, Long> {
    // Additional query methods if needed
}
