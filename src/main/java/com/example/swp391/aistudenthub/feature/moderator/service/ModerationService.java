package com.example.swp391.aistudenthub.feature.moderator.service;

import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.moderator.dto.DashboardStatsDto;
import com.example.swp391.aistudenthub.feature.moderator.entity.Moderation;

import java.util.UUID;
import java.util.List;

public interface ModerationService {

    /**
     * Get moderation dashboard statistics
     */
    DashboardStatsDto getDashboardStats(UUID moderatorId);

    /**
     * Approve a document
     */
    Moderation approveDocument(UUID documentId, UUID moderatorId, User moderator, Document document);

    /**
     * Reject a document
     */
    Moderation rejectDocument(UUID documentId, UUID moderatorId, User moderator, Document document, String reason);

    /**
     * Get all pending documents for moderation
     */
    List<Document> getPendingDocuments();

    /**
     * Get moderation history by moderator
     */
    List<Moderation> getModerationHistoryByModeratorId(UUID moderatorId);

    /**
     * Takedown a document due to DMCA/Copyright violation
     */
    Moderation takedownDocument(UUID documentId, UUID moderatorId, User moderator, Document document);
}
