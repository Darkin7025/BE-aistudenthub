package com.example.swp391.aistudenthub.feature.moderator.repository;

import com.example.swp391.aistudenthub.feature.moderator.entity.Moderation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ModerationRepository extends JpaRepository<Moderation, Long> {

    /**
     * Count approved moderations for a specific moderator
     */
    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.moderator.id = :moderatorId AND m.action = 'APPROVED'")
    Long countApprovedByModeratorId(@Param("moderatorId") UUID moderatorId);

    /**
     * Count rejected moderations for a specific moderator
     */
    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.moderator.id = :moderatorId AND m.action = 'REJECTED'")
    Long countRejectedByModeratorId(@Param("moderatorId") UUID moderatorId);

    /**
     * Count approved moderations across all moderators
     */
    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.action = 'APPROVED'")
    Long countTotalApproved();

    /**
     * Count rejected moderations across all moderators
     */
    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.action = 'REJECTED'")
    Long countTotalRejected();

    /**
     * Get all moderations for a specific moderator with pagination
     */
    @Query("SELECT m FROM Moderation m WHERE m.moderator.id = :moderatorId ORDER BY m.createdAt DESC")
    Page<Moderation> findByModeratorIdOrderByCreatedAtDesc(@Param("moderatorId") UUID moderatorId, Pageable pageable);

    /**
     * Get all moderations for a specific document
     */
    @Query("SELECT m FROM Moderation m WHERE m.document.id = :documentId ORDER BY m.createdAt DESC")
    List<Moderation> findByDocumentIdOrderByCreatedAtDesc(@Param("documentId") UUID documentId);

    /**
     * Check if document has been moderated
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Moderation m WHERE m.document.id = :documentId")
    Boolean existsByDocumentId(@Param("documentId") UUID documentId);

    /**
     * Get moderation by document and moderator
     */
    @Query("SELECT m FROM Moderation m WHERE m.document.id = :documentId AND m.moderator.id = :moderatorId")
    List<Moderation> findByDocumentAndModerator(@Param("documentId") UUID documentId, @Param("moderatorId") UUID moderatorId);
}
