package com.example.swp391.aistudenthub.feature.moderator.controller;

import com.example.swp391.aistudenthub.common.dto.ApiResponse;
import com.example.swp391.aistudenthub.exception.AppException;
import com.example.swp391.aistudenthub.exception.ErrorCode;
import com.example.swp391.aistudenthub.feature.moderator.dto.*;
import com.example.swp391.aistudenthub.feature.moderator.service.ModerationService;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.auth.service.EmailService;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Moderator API — Dashboard thống kê.
 * Cung cấp số liệu liên quan đến công việc duyệt tài liệu của moderator.
 */
@RestController
@RequestMapping("/api/v1/moderator/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
@Tag(name = "Moderator - Dashboard", description = "Thống kê dashboard cho Moderator")
@Slf4j
public class ModeratorDashboardController {

    private final ModerationService moderationService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * GET /api/v1/moderator/dashboard/stats
     * Lấy số liệu thống kê bao gồm:
     * - Số tài liệu chờ duyệt (visibility = PENDING)
     * - Số tài liệu đã duyệt của moderator hiện tại
     * - Số tài liệu bị từ chối của moderator hiện tại
     * - Tổng số tài liệu đã duyệt của tất cả moderators
     * - Tổng số tài liệu bị từ chối của tất cả moderators
     */
    @GetMapping("/stats")
    @Operation(summary = "Tổng quan thống kê duyệt tài liệu", description = "Trả về số lượng tài liệu chờ duyệt, đã duyệt và bị từ chối cho moderator hiện tại")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {
        // Extract moderator ID from currentUser
        UUID moderatorId = currentUser.getId();

        DashboardStatsDto stats = moderationService.getDashboardStats(moderatorId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * GET /api/v1/moderator/dashboard/pending-documents
     * Lấy danh sách tài liệu chờ duyệt
     */
    @GetMapping("/pending-documents")
    @Operation(summary = "Danh sách tài liệu chờ duyệt", description = "Trả về danh sách tất cả tài liệu có visibility = PENDING")
    public ResponseEntity<ApiResponse<List<PendingDocumentResponseDto>>> getPendingDocuments() {
        log.info("Fetching all pending documents");

        List<Document> pendingDocs = moderationService.getPendingDocuments();

        List<PendingDocumentResponseDto> response = pendingDocs.stream()
                .map(doc -> {
                    User uploader = userRepository.findById(doc.getUserId()).orElse(null);
                    return PendingDocumentResponseDto.builder()
                            .documentId(doc.getId())
                            .title(doc.getTitle())
                            .description(doc.getDescription())
                            .fileName(doc.getFileName())
                            .fileSize(doc.getFileSize())
                            .fileType(doc.getFileType())
                            .uploadedBy(doc.getUserId())
                            .uploaderName(uploader != null ? uploader.getFullName() : "Unknown")
                            .subject(doc.getSubject())
                            .major(doc.getMajor())
                            .documentType(doc.getDocumentType())
                            .createdAt(doc.getCreatedAt())
                            .updatedAt(doc.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/moderator/dashboard/approve
     * Phê duyệt tài liệu
     */
    @PostMapping("/approve")
    @Operation(summary = "Phê duyệt tài liệu", description = "Phê duyệt một tài liệu chờ duyệt, cập nhật visibility thành PUBLIC")
    public ResponseEntity<ApiResponse<ModerationResponseDto>> approveDocument(
            @RequestBody ApproveDocumentRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {

        UUID moderatorId = currentUser.getId();
        log.info("Approving document {} by moderator {}", request.getDocumentId(), moderatorId);

        // Get document
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Get moderator
        User moderator = currentUser;

        // Approve document
        var moderation = moderationService.approveDocument(request.getDocumentId(), moderatorId, moderator, document);

        ModerationResponseDto response = ModerationResponseDto.builder()
                .moderationId(moderation.getModerationId())
                .documentId(moderation.getDocument().getId())
                .documentTitle(moderation.getDocument().getTitle())
                .moderatorId(moderation.getModerator().getId())
                .moderatorName(moderation.getModerator().getFullName())
                .action(moderation.getAction().toString())
                .reason(moderation.getReason())
                .createdAt(moderation.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/moderator/dashboard/reject
     * Từ chối tài liệu
     */
    @PostMapping("/reject")
    @Operation(summary = "Từ chối tài liệu", description = "Từ chối một tài liệu chờ duyệt, cập nhật visibility thành PRIVATE và lưu lý do")
    public ResponseEntity<ApiResponse<ModerationResponseDto>> rejectDocument(
            @RequestBody RejectDocumentRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {

        UUID moderatorId = currentUser.getId();
        log.info("Rejecting document {} by moderator {}", request.getDocumentId(), moderatorId);

        // Get document
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Get moderator
        User moderator = currentUser;
        User user = userRepository.findById(document.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Reject document
        var moderation = moderationService.rejectDocument(request.getDocumentId(), moderatorId, moderator, document,
                request.getReason());
        emailService.sendDocumentTakedownEmail(
                user.getEmail(),
                user.getFullName(),
                document.getTitle(),
                request.getReason());
        ModerationResponseDto response = ModerationResponseDto.builder()
                .moderationId(moderation.getModerationId())
                .documentId(moderation.getDocument().getId())
                .documentTitle(moderation.getDocument().getTitle())
                .moderatorId(moderation.getModerator().getId())
                .moderatorName(moderation.getModerator().getFullName())
                .action(moderation.getAction().toString())
                .reason(moderation.getReason())
                .createdAt(moderation.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/moderator/dashboard/moderation-history
     * Lấy lịch sử duyệt tài liệu của moderator hiện tại
     */
    @GetMapping("/moderation-history")
    @Operation(summary = "Lịch sử duyệt tài liệu", description = "Trả về danh sách tất cả tài liệu đã duyệt bởi moderator hiện tại")
    public ResponseEntity<ApiResponse<List<ModerationResponseDto>>> getModerationHistory(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {
        UUID moderatorId = currentUser.getId();
        log.info("Fetching moderation history for moderator {}", moderatorId);

        var moderations = moderationService.getModerationHistoryByModeratorId(moderatorId);

        List<ModerationResponseDto> response = moderations.stream()
                .map(m -> ModerationResponseDto.builder()
                        .moderationId(m.getModerationId())
                        .documentId(m.getDocument().getId())
                        .documentTitle(m.getDocument().getTitle())
                        .moderatorId(m.getModerator().getId())
                        .moderatorName(m.getModerator().getFullName())
                        .action(m.getAction().toString())
                        .reason(m.getReason())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/moderator/dashboard/dmca-takedown
     * Gỡ bỏ tài liệu vi phạm bản quyền (DMCA Takedown)
     */
    @PostMapping("/dmca-takedown")
    @Operation(summary = "Gỡ bỏ tài liệu vi phạm bản quyền (DMCA Takedown)", description = "Gỡ bỏ tài liệu khẩn cấp và xóa các vector chunks của AI")
    public ResponseEntity<ApiResponse<ModerationResponseDto>> takedownDocument(
            @RequestBody com.example.swp391.aistudenthub.feature.moderator.dto.TakedownDocumentRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {

        UUID moderatorId = currentUser.getId();
        log.info("DMCA takedown for document {} by moderator {}", request.getDocumentId(), moderatorId);

        // Get document
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Get moderator
        User moderator = currentUser;

        // Takedown document
        var moderation = moderationService.takedownDocument(request.getDocumentId(), moderatorId, moderator, document);
        User user = userRepository.findById(document.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        emailService.sendDocumentTakedownEmail(
                user.getEmail(),
                user.getFullName(),
                document.getTitle(),
                moderation.getReason());        
        ModerationResponseDto response = ModerationResponseDto.builder()
                .moderationId(moderation.getModerationId())
                .documentId(moderation.getDocument().getId())
                .documentTitle(moderation.getDocument().getTitle())
                .moderatorId(moderation.getModerator().getId())
                .moderatorName(moderation.getModerator().getFullName())
                .action(moderation.getAction().toString())
                .reason(moderation.getReason())
                .createdAt(moderation.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
