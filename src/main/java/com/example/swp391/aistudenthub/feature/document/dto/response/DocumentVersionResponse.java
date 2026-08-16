package com.example.swp391.aistudenthub.feature.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionResponse {
    private UUID id;
    private UUID documentId;
    private Integer versionNumber;
    private String content;
    private String fileUrl;
    private UUID createdBy;
    private OffsetDateTime createdAt;
}
