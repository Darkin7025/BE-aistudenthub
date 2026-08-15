package com.example.swp391.aistudenthub.feature.moderator.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakedownDocumentRequest {

    @NotNull(message = "documentId không được để trống")
    private UUID documentId;
}
