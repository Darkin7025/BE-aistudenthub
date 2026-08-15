package com.example.swp391.aistudenthub.feature.payment.service;

import com.example.swp391.aistudenthub.feature.payment.entity.PaymentOrder;
import com.example.swp391.aistudenthub.feature.payment.entity.PricingPlan;
import com.example.swp391.aistudenthub.feature.payment.enums.PaymentStatus;
import com.example.swp391.aistudenthub.feature.payment.repository.PaymentOrderRepository;
import com.example.swp391.aistudenthub.feature.payment.repository.PricingPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPlanResolverService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PricingPlanRepository pricingPlanRepository;

    public record UserPlanLimits(
            String planName,
            int aiDailyLimit,
            int documentLimit,
            boolean isPremium
    ) {}

    public UserPlanLimits resolveLimits(UUID userId) {
        Optional<PaymentOrder> latestPaidOrder = paymentOrderRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, PaymentStatus.PAID);

        if (latestPaidOrder.isEmpty()) {
            return new UserPlanLimits("Cơ bản", 20, 50, false); // Default limits
        }

        PaymentOrder order = latestPaidOrder.get();
        OffsetDateTime paidAt = order.getPaidAt();

        if (order.getPlanId() != null) {
            Optional<PricingPlan> planOpt = pricingPlanRepository.findById(order.getPlanId());
            if (planOpt.isPresent()) {
                PricingPlan plan = planOpt.get();
                int durationMonths = plan.getDurationMonths();
                if (paidAt != null && paidAt.plusMonths(durationMonths).isAfter(OffsetDateTime.now())) {
                    return new UserPlanLimits(
                            plan.getName(),
                            plan.getAiDailyLimit(),
                            plan.getDocumentLimit(),
                            true
                    );
                }
            }
        }

        // Backward compatibility for orders without planId
        if (paidAt != null && paidAt.plusMonths(1).isAfter(OffsetDateTime.now())) {
            int amount = order.getAmount();
            if (amount >= 79000) {
                return new UserPlanLimits("Chuyên gia", 100, Integer.MAX_VALUE, true);
            } else if (amount >= 39000) {
                return new UserPlanLimits("Nâng cao", 50, 500, true);
            }
        }

        return new UserPlanLimits("Cơ bản", 20, 50, false);
    }
}
