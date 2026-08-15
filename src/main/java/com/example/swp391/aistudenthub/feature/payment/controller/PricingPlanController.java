package com.example.swp391.aistudenthub.feature.payment.controller;

import com.example.swp391.aistudenthub.common.dto.ApiResponse;
import com.example.swp391.aistudenthub.feature.payment.dto.response.PricingPlanResponse;
import com.example.swp391.aistudenthub.feature.payment.service.PricingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing-plans")
@RequiredArgsConstructor
@Tag(name = "Pricing Plans", description = "Các gói thanh toán hoạt động hiển thị cho người dùng")
public class PricingPlanController {

    private final PricingPlanService pricingPlanService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các gói thanh toán đang hoạt động")
    public ResponseEntity<ApiResponse<List<PricingPlanResponse>>> getActivePlans() {
        List<PricingPlanResponse> activePlans = pricingPlanService.getActivePlans();
        return ResponseEntity.ok(ApiResponse.success(activePlans));
    }
}
