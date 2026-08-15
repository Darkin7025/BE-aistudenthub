package com.example.swp391.aistudenthub.feature.payment.service;

import com.example.swp391.aistudenthub.exception.AppException;
import com.example.swp391.aistudenthub.exception.ErrorCode;
import com.example.swp391.aistudenthub.feature.payment.dto.request.PricingPlanRequest;
import com.example.swp391.aistudenthub.feature.payment.dto.response.PricingPlanResponse;
import com.example.swp391.aistudenthub.feature.payment.entity.PricingPlan;
import com.example.swp391.aistudenthub.feature.payment.repository.PricingPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingPlanService {

    private final PricingPlanRepository pricingPlanRepository;

    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getAllPlans() {
        return pricingPlanRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricingPlanResponse> getActivePlans() {
        return pricingPlanRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PricingPlanResponse getPlanById(UUID id) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Gói cước không tồn tại"));
        return toResponse(plan);
    }

    @Transactional
    public PricingPlanResponse createPlan(PricingPlanRequest request) {
        PricingPlan plan = PricingPlan.builder()
                .name(request.getName().trim())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .aiDailyLimit(request.getAiDailyLimit())
                .documentLimit(request.getDocumentLimit())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        PricingPlan saved = pricingPlanRepository.save(plan);
        log.info("Created pricing plan: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public PricingPlanResponse updatePlan(UUID id, PricingPlanRequest request) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Gói cước không tồn tại"));

        plan.setName(request.getName().trim());
        plan.setPrice(request.getPrice());
        plan.setDurationMonths(request.getDurationMonths());
        plan.setAiDailyLimit(request.getAiDailyLimit());
        plan.setDocumentLimit(request.getDocumentLimit());
        plan.setDescription(request.getDescription());
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }

        PricingPlan saved = pricingPlanRepository.save(plan);
        log.info("Updated pricing plan: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void deletePlan(UUID id) {
        PricingPlan plan = pricingPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Gói cước không tồn tại"));
        try {
            pricingPlanRepository.delete(plan);
            log.info("Deleted pricing plan: {}", id);
        } catch (Exception e) {
            log.warn("Failed to hard delete pricing plan {}, deactivating instead: {}", id, e.getMessage());
            plan.setActive(false);
            pricingPlanRepository.save(plan);
        }
    }

    private PricingPlanResponse toResponse(PricingPlan plan) {
        return PricingPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .durationMonths(plan.getDurationMonths())
                .aiDailyLimit(plan.getAiDailyLimit())
                .documentLimit(plan.getDocumentLimit())
                .description(plan.getDescription())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
