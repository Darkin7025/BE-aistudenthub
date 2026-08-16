package com.example.swp391.aistudenthub.feature.document.repository;

import com.example.swp391.aistudenthub.feature.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId);

    Optional<DocumentVersion> findTopByDocumentIdOrderByVersionNumberDesc(UUID documentId);

    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber);
}
