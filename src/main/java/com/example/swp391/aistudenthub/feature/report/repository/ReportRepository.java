package com.example.swp391.aistudenthub.feature.report.repository;

import com.example.swp391.aistudenthub.feature.report.entity.Report;
import com.example.swp391.aistudenthub.feature.report.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByDocument_IdAndReporter_IdAndStatus(UUID documentId, UUID reporterId, ReportStatus status);

    boolean existsByDocumentIdAndReporterIdAndStatus(UUID documentId, UUID reporterId, ReportStatus status);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findAllByOrderByCreatedAtDesc();

    long countByStatus(ReportStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Report r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:reason IS NULL OR r.reason = :reason)")
    org.springframework.data.domain.Page<Report> searchReports(
            @org.springframework.data.repository.query.Param("status") com.example.swp391.aistudenthub.feature.report.enums.ReportStatus status,
            @org.springframework.data.repository.query.Param("reason") com.example.swp391.aistudenthub.feature.report.enums.ReportReason reason,
            org.springframework.data.domain.Pageable pageable);
}
