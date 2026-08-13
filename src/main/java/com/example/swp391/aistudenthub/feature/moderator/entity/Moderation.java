package com.example.swp391.aistudenthub.feature.moderator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.auth.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "moderation",
    indexes = {
        @Index(name = "idx_moderation_document", columnList = "document_id"),
        @Index(name = "idx_moderation_moderator", columnList = "moderator_id"),
        @Index(name = "idx_moderation_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moderation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moderation_id")
    private Long moderationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_moderation_document"))
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false, foreignKey = @ForeignKey(name = "fk_moderation_moderator"))
    private User moderator;

    @Column(name = "action", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ModerationAction action;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ModerationAction {
        APPROVED,
        REJECTED
    }
}
