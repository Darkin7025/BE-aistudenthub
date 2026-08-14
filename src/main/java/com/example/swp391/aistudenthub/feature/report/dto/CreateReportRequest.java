package com.example.swp391.aistudenthub.feature.report.dto;

import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    @NotNull(message = "documentId không được để trống")
    private UUID documentId;

    @NotNull(message = "reason không được để trống")
    private ReportReason reason;

    private String description;
}
