package com.example.swp391.aistudenthub.feature.report.service;

import com.example.swp391.aistudenthub.exception.AppException;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.ApprovalStatus;
import com.example.swp391.aistudenthub.feature.document.enums.DocumentVisibility;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import com.example.swp391.aistudenthub.feature.report.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.swp391.aistudenthub.feature.document.service.DocumentService documentService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void createReport_shouldSavePendingReportOnlyForPublicDocument() {
        UUID documentId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        Document document = new Document();
        document.setId(documentId);
        document.setUserId(UUID.randomUUID());
        document.setVisibility(DocumentVisibility.PUBLIC);
        document.setApprovalStatus(ApprovalStatus.APPROVED);
        document.setTitle("Test doc");

        when(documentRepository.findByIdAndDeletedAtIsNull(documentId)).thenReturn(Optional.of(document));
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(new User()));
        when(reportRepository.existsByDocument_IdAndReporter_IdAndStatus(documentId, reporterId, ReportStatus.PENDING)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report report = reportService.createReport(documentId, reporterId, ReportReason.SPAM, "spam content");

        assertNotNull(report);
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertEquals(ReportReason.SPAM, report.getReason());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void resolveReport_shouldTakedownDocumentWhenReportIsValid() {
        UUID documentId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        Document document = new Document();
        document.setId(documentId);
        document.setVisibility(DocumentVisibility.PUBLIC);
        document.setApprovalStatus(ApprovalStatus.APPROVED);

        User moderator = new User();
        moderator.setId(moderatorId);

        Report report = new Report();
        report.setId(1L);
        report.setDocument(document);
        report.setStatus(ReportStatus.PENDING);
        report.setReason(ReportReason.COPYRIGHT_VIOLATION);
        report.setDescription("It is copied");

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report resolved = reportService.resolveReport(1L, moderator, "MAKE_PRIVATE", "Removed for copyright issue");

        assertEquals(ReportStatus.RESOLVED, resolved.getStatus());
        verify(documentService, times(1)).takedownDocument(documentId, moderatorId);
        assertEquals(moderatorId, resolved.getReviewedBy().getId());
        assertNotNull(resolved.getReviewedAt());
    }

    @Test
    void createReport_shouldRejectPrivateDocument() {
        UUID documentId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        Document document = new Document();
        document.setId(documentId);
        document.setUserId(UUID.randomUUID());
        document.setVisibility(DocumentVisibility.PRIVATE);

        when(documentRepository.findByIdAndDeletedAtIsNull(documentId)).thenReturn(Optional.of(document));

        assertThrows(AppException.class, () -> reportService.createReport(documentId, reporterId, ReportReason.OTHER, "test"));
        verify(reportRepository, never()).save(any());
    }
}
