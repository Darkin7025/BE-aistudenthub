package com.example.swp391.aistudenthub.feature.report.controller;

import com.example.swp391.aistudenthub.common.dto.ApiResponse;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.report.dto.ResolveReportRequest;
import com.example.swp391.aistudenthub.feature.report.dto.ReportResponse;
import com.example.swp391.aistudenthub.feature.report.dto.SubmitReportRequest;
import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import com.example.swp391.aistudenthub.feature.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.swp391.aistudenthub.feature.report.repository.ReportRepository;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Report & Moderation", description = "Các API báo cáo vi phạm và giải quyết báo cáo")
@org.springframework.transaction.annotation.Transactional
public class AdminReportController {

    private final ReportService reportService;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;

    @GetMapping("/api/v1/admin/reports/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Admin/Moderator xem thống kê báo cáo vi phạm")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getReportStats() {
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long resolved = reportRepository.countByStatus(ReportStatus.RESOLVED);
        long dismissed = reportRepository.countByStatus(ReportStatus.DISMISSED);
        long total = pending + resolved + dismissed;

        Map<String, Long> stats = Map.of(
            "totalReports", total,
            "pendingReports", pending,
            "resolvedReports", resolved,
            "dismissedReports", dismissed
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/api/v1/documents/{id}/report")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Sinh viên gửi báo cáo vi phạm cho tài liệu")
    public ResponseEntity<ApiResponse<ReportResponse>> submitReport(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitReportRequest request,
            @AuthenticationPrincipal User currentUser) {
        Report report = reportService.createReport(id, currentUser.getId(), request.getReason(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(report), "Gửi báo cáo vi phạm thành công."));
    }

    @GetMapping("/api/v1/admin/reports")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Admin/Moderator xem danh sách báo cáo vi phạm")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<ReportResponse> result = reportService.searchReports(status, reason, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/api/v1/admin/reports/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @Operation(summary = "Admin/Moderator giải quyết báo cáo vi phạm")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal User currentUser) {
        Report report = reportService.reviewReport(reportId, currentUser, request.getDecision(), request.getModeratorNote());
        return ResponseEntity.ok(ApiResponse.success(toResponse(report), "Đã xử lý báo cáo vi phạm thành công."));
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
