package com.example.swp391.aistudenthub.feature.moderator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    
    /**
     * Number of documents pending review (visibility = PENDING)
     */
    private Long pendingDocuments;
    
    /**
     * Number of documents approved by current moderator
     */
    private Long approvedDocuments;
    
    /**
     * Number of documents rejected by current moderator
     */
    private Long rejectedDocuments;
    
    /**
     * Total number of documents approved by all moderators
     */
    private Long totalApprovedDocuments;
    
    /**
     * Total number of documents rejected by all moderators
     */
    private Long totalRejectedDocuments;
}
