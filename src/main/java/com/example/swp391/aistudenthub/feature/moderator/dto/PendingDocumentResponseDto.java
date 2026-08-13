package com.example.swp391.aistudenthub.feature.moderator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDocumentResponseDto {
    
    private UUID documentId;
    
    private String title;
    
    private String description;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileType;
    
    private UUID uploadedBy;
    
    private String uploaderName;
    
    private String subject;
    
    private String major;
    
    private String documentType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime updatedAt;
}
