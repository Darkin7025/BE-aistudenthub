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
public class ApproveDocumentRequest {
    
    /**
     * ID of the document to approve
     */
    private UUID documentId;
}
