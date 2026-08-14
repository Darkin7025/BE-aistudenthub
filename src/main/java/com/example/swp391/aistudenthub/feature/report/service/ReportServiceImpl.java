package com.example.swp391.aistudenthub.feature.report.service;

import com.example.swp391.aistudenthub.exception.AppException;
import com.example.swp391.aistudenthub.exception.ErrorCode;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.DocumentVisibility;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import com.example.swp391.aistudenthub.feature.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Report createReport(UUID documentId, UUID reporterId, ReportReason reason, String description) {
        Document document = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (!DocumentVisibility.PUBLIC.equals(document.getVisibility())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể report tài liệu đang ở trạng thái PUBLIC.");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (reportRepository.existsByDocument_IdAndReporter_IdAndStatus(documentId, reporterId, ReportStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Bạn đã report tài liệu này và đang chờ xử lý.");
        }

        Report report = Report.builder()
                .document(document)
                .reporter(reporter)
                .reason(reason)
                .description(description)
                .status(ReportStatus.PENDING)
                .build();

        log.info("Creating report for document {} by reporter {}", documentId, reporterId);
        return reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
    }

    @Override
    @Transactional
    public Report reviewReport(Long reportId, User moderator, ReportStatus decision, String moderatorNote) {
        if (moderator == null || moderator.getId() == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Moderator không hợp lệ.");
        }

        if (decision == null || (!ReportStatus.RESOLVED.equals(decision) && !ReportStatus.DISMISSED.equals(decision))) {
            throw new AppException(ErrorCode.INVALID_REPORT_STATUS, "Quyết định review phải là RESOLVED hoặc DISMISSED.");
        }

        return resolveReportByStatus(reportId, moderator, decision, moderatorNote);
    }

    @Override
    @Transactional
    public Report resolveReport(Long reportId, User moderator, String action, String moderatorNote) {
        if (moderator == null || moderator.getId() == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Moderator không hợp lệ.");
        }

        ReportStatus decision = "MAKE_PRIVATE".equalsIgnoreCase(action) ? ReportStatus.RESOLVED : ReportStatus.DISMISSED;
        return resolveReportByStatus(reportId, moderator, decision, moderatorNote);
    }

    private Report resolveReportByStatus(Long reportId, User moderator, ReportStatus decision, String moderatorNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        if (!ReportStatus.PENDING.equals(report.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REPORT_STATUS, "Report này đã được xử lý trước đó.");
        }

        report.setReviewedBy(moderator);
        report.setReviewedAt(OffsetDateTime.now());
        report.setModeratorNote(moderatorNote);

        if (ReportStatus.RESOLVED.equals(decision)) {
            Document document = report.getDocument();
            document.setVisibility(DocumentVisibility.PRIVATE);
            documentRepository.save(document);
            report.setStatus(ReportStatus.RESOLVED);
        } else {
            report.setStatus(ReportStatus.DISMISSED);
        }

        log.info("Report {} reviewed by moderator {} with decision {}", reportId, moderator.getId(), decision);
        return reportRepository.save(report);
    }
}
