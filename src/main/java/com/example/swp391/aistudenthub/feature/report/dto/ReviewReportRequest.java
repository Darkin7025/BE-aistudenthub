package com.example.swp391.aistudenthub.feature.report.dto;

import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportRequest {

    @NotNull(message = "decision không được để trống")
    private ReportStatus decision;

    private String moderatorNote;
}
