package com.example.swp391.aistudenthub.feature.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlanRequest {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    @NotNull(message = "Giá gói không được để trống")
    @Min(value = 0, message = "Giá gói phải lớn hơn hoặc bằng 0")
    private Integer price;

    @NotNull(message = "Thời hạn không được để trống")
    @Min(value = 1, message = "Thời hạn phải lớn hơn hoặc bằng 1 tháng")
    private Integer durationMonths;

    @NotNull(message = "Giới hạn câu hỏi AI mỗi ngày không được để trống")
    @Min(value = 0, message = "Giới hạn câu hỏi AI không được âm")
    private Integer aiDailyLimit;

    @NotNull(message = "Giới hạn số tài liệu không được để trống")
    @Min(value = 0, message = "Giới hạn số tài liệu không được âm")
    private Integer documentLimit;

    private String description;

    @Builder.Default
    private Boolean active = true;
}
