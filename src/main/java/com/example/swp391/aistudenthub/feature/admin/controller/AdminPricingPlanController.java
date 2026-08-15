package com.example.swp391.aistudenthub.feature.admin.controller;

import com.example.swp391.aistudenthub.common.dto.ApiResponse;
import com.example.swp391.aistudenthub.common.dto.MessageResponse;
import com.example.swp391.aistudenthub.feature.payment.dto.request.PricingPlanRequest;
import com.example.swp391.aistudenthub.feature.payment.dto.response.PricingPlanResponse;
import com.example.swp391.aistudenthub.feature.payment.service.PricingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pricing-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Pricing Plans", description = "Quản lý các gói thanh toán động của hệ thống")
public class AdminPricingPlanController {

    private final PricingPlanService pricingPlanService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các gói thanh toán")
    public ResponseEntity<ApiResponse<List<PricingPlanResponse>>> getAllPlans() {
        List<PricingPlanResponse> plans = pricingPlanService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết gói thanh toán theo ID")
    public ResponseEntity<ApiResponse<PricingPlanResponse>> getPlanById(@PathVariable UUID id) {
        PricingPlanResponse plan = pricingPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping
    @Operation(summary = "Tạo mới một gói thanh toán")
    public ResponseEntity<ApiResponse<PricingPlanResponse>> createPlan(
            @Valid @RequestBody PricingPlanRequest request) {
        PricingPlanResponse created = pricingPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Gói thanh toán đã được tạo thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin gói thanh toán")
    public ResponseEntity<ApiResponse<PricingPlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody PricingPlanRequest request) {
        PricingPlanResponse updated = pricingPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật gói thanh toán thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa (hoặc vô hiệu hóa) gói thanh toán")
    public ResponseEntity<ApiResponse<MessageResponse>> deletePlan(@PathVariable UUID id) {
        pricingPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success(new MessageResponse("Xóa gói thanh toán thành công")));
    }
}
