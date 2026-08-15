package com.example.swp391.aistudenthub.feature.document.mapper;

import com.example.swp391.aistudenthub.feature.document.dto.response.DocumentResponse;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.PreviewMode;
import com.example.swp391.aistudenthub.feature.document.service.DocumentPreviewResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final DocumentPreviewResolver previewResolver;
    private final com.example.swp391.aistudenthub.feature.auth.repository.UserRepository userRepository;

    public DocumentResponse toResponse(Document doc) {
        if (doc == null) {
            return null;
        }

        PreviewMode previewMode = previewResolver.resolveMode(doc.getOriginalFileName(), doc.getFileType());
        boolean aiSupported = previewResolver.isAiCapable(previewMode) && StringUtils.hasText(doc.getExtractedText());

        String creatorName = null;
        String creatorEmail = null;
        if (doc.getUserId() != null) {
            var creatorOpt = userRepository.findByIdAndDeletedAtIsNull(doc.getUserId());
            creatorName = creatorOpt.map(com.example.swp391.aistudenthub.feature.auth.entity.User::getFullName).orElse("Unknown");
            creatorEmail = creatorOpt.map(com.example.swp391.aistudenthub.feature.auth.entity.User::getEmail).orElse(null);
        }

        return DocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .fileUrl(doc.getFileUrl())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .fileType(doc.getFileType())
                .previewMode(previewMode.name())
                .aiSupported(aiSupported)
                .visibility(doc.getVisibility())
                .subject(doc.getSubject())
                .major(doc.getMajor())
                .documentType(doc.getDocumentType())
                .folderId(doc.getFolderId())
                .uploadStatus(doc.getUploadStatus())
                .uploadProgress(doc.getUploadProgress())
                .createdAt(doc.getCreatedAt())
                .creatorId(doc.getUserId())
                .creatorName(creatorName)
                .creatorEmail(creatorEmail)
                .customMetadata(doc.getCustomMetadata())
                .extractedText(doc.getExtractedText())
                .approvalStatus(doc.getApprovalStatus())
                .dmcaVerified(doc.getDmcaVerified())
                .dmcaVerifiedAt(doc.getDmcaVerifiedAt())
                .dmcaVerifiedBy(doc.getDmcaVerifiedBy())
                .rejectionReason(doc.getRejectionReason())
                .build();
    }

    public DocumentResponse toResponse(Document doc, com.example.swp391.aistudenthub.feature.auth.entity.User sharedByUser) {
        DocumentResponse response = toResponse(doc);
        if (response != null && sharedByUser != null) {
            response.setSharedByUserId(sharedByUser.getId());
            response.setSharedByUserName(sharedByUser.getFullName());
            response.setSharedByUserEmail(sharedByUser.getEmail());
        }
        return response;
    }
}
