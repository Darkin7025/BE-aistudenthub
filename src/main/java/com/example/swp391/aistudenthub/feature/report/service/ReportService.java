package com.example.swp391.aistudenthub.feature.report.service;

import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.enums.ReportReason;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    Report createReport(UUID documentId, UUID reporterId, ReportReason reason, String description);

    List<Report> getPendingReports();

    Report reviewReport(Long reportId, User moderator, ReportStatus decision, String moderatorNote);

    Report resolveReport(Long reportId, User moderator, String action, String moderatorNote);
}
