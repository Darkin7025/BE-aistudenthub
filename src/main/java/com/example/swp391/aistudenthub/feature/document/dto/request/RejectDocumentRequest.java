package com.example.swp391.aistudenthub.feature.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectDocumentRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String rejectionReason;
}
