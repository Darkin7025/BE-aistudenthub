package com.example.swp391.aistudenthub.feature.report.dto;

import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long reportId;
    private UUID documentId;
    private String documentTitle;
    private UUID reporterId;
    private String reporterName;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private UUID reviewedBy;
    private String moderatorName;
    private OffsetDateTime reviewedAt;
    private String moderatorNote;
    private OffsetDateTime createdAt;
}
