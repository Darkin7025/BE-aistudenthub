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
    ) {
    }

    public UserPlanLimits resolveLimits(UUID userId) {

        Optional<PaymentOrder> latestPaidOrder =
                paymentOrderRepository
                        .findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                                userId,
                                PaymentStatus.PAID
                        );

        if (latestPaidOrder.isEmpty()) {
            return defaultPlan();
        }

        PaymentOrder order = latestPaidOrder.get();
        OffsetDateTime paidAt = order.getPaidAt();

        if (paidAt == null) {
            return defaultPlan();
        }

        /*
         * Order mới có planId
         */
        if (order.getPlanId() != null) {

            Optional<PricingPlan> planOpt =
                    pricingPlanRepository.findById(order.getPlanId());

            if (planOpt.isPresent()) {

                PricingPlan plan = planOpt.get();

                if (paidAt
                        .plusMonths(plan.getDurationMonths())
                        .isAfter(OffsetDateTime.now())) {

                    return new UserPlanLimits(
                            plan.getName(),
                            plan.getAiDailyLimit(),
                            plan.getDocumentLimit(),
                            true
                    );
                }
            }
        }

        /*
         * Order cũ không có planId
         */
        if (paidAt.plusMonths(1).isAfter(OffsetDateTime.now())) {

            Integer amount = order.getAmount();

            /*
             * Tìm gói theo giá hiện tại trong DB.
             * Không hard-code 39.000 / 79.000.
             */
            Optional<PricingPlan> planOpt =
                    pricingPlanRepository.findByPrice(amount);

            if (planOpt.isPresent()) {

                PricingPlan plan = planOpt.get();

                String displayName;

                if ("PRO".equalsIgnoreCase(plan.getName())) {
                    displayName = "Chuyên gia";
                } else {
                    displayName = "Nâng cao";
                }

                return new UserPlanLimits(
                        displayName,
                        plan.getAiDailyLimit(),
                        plan.getDocumentLimit(),
                        true
                );
            }
        }

        return defaultPlan();
    }

    private UserPlanLimits defaultPlan() {
        return new UserPlanLimits(
                "Cơ bản",
                20,
                50,
                false
        );
    }
}