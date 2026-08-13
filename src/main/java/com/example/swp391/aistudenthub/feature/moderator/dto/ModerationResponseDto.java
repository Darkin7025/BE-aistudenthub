package com.example.swp391.aistudenthub.feature.moderator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationResponseDto {
    
    private Long moderationId;
    
    private UUID documentId;
    
    private String documentTitle;
    
    private UUID moderatorId;
    
    private String moderatorName;
    
    private String action;
    
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
