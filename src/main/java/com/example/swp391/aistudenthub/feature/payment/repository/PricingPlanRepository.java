package com.example.swp391.aistudenthub.feature.payment.repository;

import com.example.swp391.aistudenthub.feature.payment.entity.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, UUID> {
    List<PricingPlan> findByActiveTrue();
}
