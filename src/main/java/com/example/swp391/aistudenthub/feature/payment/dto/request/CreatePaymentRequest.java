package com.example.swp391.aistudenthub.feature.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private UUID planId;

    private Integer amount;

    private String description;

    private String returnUrl;
    private String cancelUrl;
}
