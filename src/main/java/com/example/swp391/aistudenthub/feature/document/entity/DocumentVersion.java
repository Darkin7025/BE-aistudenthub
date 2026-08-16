package com.example.swp391.aistudenthub.feature.document.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "document_versions",
        indexes = {
                @Index(name = "idx_document_versions_document_number", columnList = "document_id, version_number"),
                @Index(name = "idx_document_versions_document_created", columnList = "document_id, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url", length = 2000)
    private String fileUrl;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
