package com.example.swp391.aistudenthub.feature.moderator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectDocumentRequest {
    
    /**
     * ID of the document to reject
     */
    private UUID documentId;
    
    /**
     * Reason for rejection
     */
    private String reason;
}
