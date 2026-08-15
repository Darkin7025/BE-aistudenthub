package com.example.swp391.aistudenthub.feature.payment.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlanResponse {
    private UUID id;
    private String name;
    private Integer price;
    private Integer durationMonths;
    private Integer aiDailyLimit;
    private Integer documentLimit;
    private String description;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
