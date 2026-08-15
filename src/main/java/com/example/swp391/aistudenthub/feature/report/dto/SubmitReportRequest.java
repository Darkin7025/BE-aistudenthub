package com.example.swp391.aistudenthub.feature.report.dto;

import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReportRequest {

    @NotNull(message = "reason không được để trống")
    private ReportReason reason;

    private String description;
}
