package com.example.swp391.aistudenthub.feature.moderator.service;

import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.DocumentVisibility;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.moderator.dto.DashboardStatsDto;
import com.example.swp391.aistudenthub.feature.moderator.entity.Moderation;
import com.example.swp391.aistudenthub.feature.moderator.repository.ModerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationServiceImpl implements ModerationService {

    private final ModerationRepository moderationRepository;
    private final DocumentRepository documentRepository;

    /**
     * Get moderation dashboard statistics
     */
    @Override
    public DashboardStatsDto getDashboardStats(UUID moderatorId) {
        Long pendingDocuments = documentRepository.countPendingDocuments();
        Long approvedByModerator = moderationRepository.countApprovedByModeratorId(moderatorId);
        Long rejectedByModerator = moderationRepository.countRejectedByModeratorId(moderatorId);
        Long totalApproved = moderationRepository.countTotalApproved();
        Long totalRejected = moderationRepository.countTotalRejected();

        return DashboardStatsDto.builder()
                .pendingDocuments(pendingDocuments)
                .approvedDocuments(approvedByModerator)
                .rejectedDocuments(rejectedByModerator)
                .totalApprovedDocuments(totalApproved)
                .totalRejectedDocuments(totalRejected)
                .build();
    }

    /**
     * Approve a document
     */
    @Override
    @Transactional
    public Moderation approveDocument(UUID documentId, UUID moderatorId, User moderator, Document document) {
        log.info("Approving document {} by moderator {}", documentId, moderatorId);

        // Update document visibility to PUBLIC
        document.setVisibility(DocumentVisibility.PUBLIC);
        documentRepository.save(document);

        // Create moderation record
        Moderation moderation = Moderation.builder()
                .document(document)
                .moderator(moderator)
                .action(Moderation.ModerationAction.APPROVED)
                .reason(null)
                .build();

        return moderationRepository.save(moderation);
    }

    /**
     * Reject a document
     */
    @Override
    @Transactional
    public Moderation rejectDocument(UUID documentId, UUID moderatorId, User moderator, Document document, String reason) {
        log.info("Rejecting document {} by moderator {}", documentId, moderatorId);

        // Update document visibility to PRIVATE
        document.setVisibility(DocumentVisibility.PRIVATE);
        documentRepository.save(document);

        // Create moderation record
        Moderation moderation = Moderation.builder()
                .document(document)
                .moderator(moderator)
                .action(Moderation.ModerationAction.REJECTED)
                .reason(reason)
                .build();

        return moderationRepository.save(moderation);
    }

    /**
     * Get all pending documents for moderation
     */
    @Override
    public List<Document> getPendingDocuments() {
        log.info("Fetching all pending documents");
        return documentRepository.findAll().stream()
                .filter(d -> d.getVisibility() == DocumentVisibility.PENDING && d.getDeletedAt() == null)
                .toList();
    }

    /**
     * Get moderation history by moderator
     */
    @Override
    public List<Moderation> getModerationHistoryByModeratorId(UUID moderatorId) {
        log.info("Fetching moderation history for moderator {}", moderatorId);
        return moderationRepository.findAll().stream()
                .filter(m -> m.getModerator().getId().equals(moderatorId))
                .toList();
    }
}
