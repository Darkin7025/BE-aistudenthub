package com.example.swp391.aistudenthub.feature.report.controller;

import com.example.swp391.aistudenthub.common.dto.ApiResponse;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.report.dto.CreateReportRequest;
import com.example.swp391.aistudenthub.feature.report.dto.ReportResponse;
import com.example.swp391.aistudenthub.feature.report.dto.ReviewReportRequest;
import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Report document APIs")
public class ReportController {

    private final ReportService reportService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Student tạo report cho document đang public")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User currentUser) {

        Report report = reportService.createReport(request.getDocumentId(), currentUser.getId(), request.getReason(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(report), "Report đã được gửi thành công"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    @Operation(summary = "Moderator xem danh sách report đang PENDING")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getPendingReports() {
        List<ReportResponse> response = reportService.getPendingReports().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{reportId}/review")
    @PreAuthorize("hasRole('MODERATOR')")
    @Operation(summary = "Moderator xử lý report, có thể DISMISSED hoặc RESOLVED")
    public ResponseEntity<ApiResponse<ReportResponse>> reviewReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReviewReportRequest request,
            @AuthenticationPrincipal User currentUser) {

        Report report = reportService.reviewReport(reportId, currentUser, request.getDecision(), request.getModeratorNote());
        return ResponseEntity.ok(ApiResponse.success(toResponse(report), "Report đã được xử lý"));
    }

    private ReportResponse toResponse(Report report) {
        Document document = documentRepository.findById(report.getDocument().getId()).orElse(null);
        return ReportResponse.builder()
                .reportId(report.getId())
                .documentId(report.getDocument().getId())
                .documentTitle(document != null ? document.getTitle() : report.getDocument().getTitle())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getFullName())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .reviewedBy(report.getReviewedBy() != null ? report.getReviewedBy().getId() : null)
                .moderatorName(report.getReviewedBy() != null ? report.getReviewedBy().getFullName() : null)
                .reviewedAt(report.getReviewedAt())
                .moderatorNote(report.getModeratorNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
