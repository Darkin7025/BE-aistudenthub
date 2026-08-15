package com.example.swp391.aistudenthub.feature.document.service;

import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.document.dto.request.UploadDocumentRequest;
import com.example.swp391.aistudenthub.feature.document.dto.response.DocumentResponse;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.ApprovalStatus;
import com.example.swp391.aistudenthub.feature.document.enums.DocumentVisibility;
import com.example.swp391.aistudenthub.feature.document.enums.UploadStatus;
import com.example.swp391.aistudenthub.feature.document.mapper.DocumentMapper;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.payment.service.UserPlanResolverService;
import com.example.swp391.aistudenthub.feature.moderator.entity.Moderation;
import com.example.swp391.aistudenthub.feature.moderator.repository.ModerationRepository;
import com.example.swp391.aistudenthub.feature.moderator.service.ModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DMCAModerationTest {

    // mocks for DocumentService upload test
    @Mock private DocumentRepository documentRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private DocumentMapper documentMapper;
    @Mock private UserPlanResolverService userPlanResolverService;
    @Mock private com.example.swp391.aistudenthub.feature.admin.repository.SystemConfigRepository systemConfigRepository;
    @InjectMocks private DocumentService documentService;

    // mocks for ModerationService tests
    @Mock private ModerationRepository moderationRepository;
    @Mock private DocumentProcessor documentProcessor;
    @InjectMocks private ModerationServiceImpl moderationService;

    private UUID userId;
    private UUID docId;
    private Document pendingDoc;
    private User moderator;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        docId = UUID.randomUUID();

        pendingDoc = Document.builder()
                .id(docId)
                .userId(userId)
                .title("Public Doc")
                .visibility(DocumentVisibility.PUBLIC)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        moderator = new User();
        moderator.setId(UUID.randomUUID());
        moderator.setFullName("Test Moderator");
    }

    @Test
    void uploadPublicDocument_ShouldDefaultToPending() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        UploadDocumentRequest request = new UploadDocumentRequest();
        request.setTitle("Public Doc");
        request.setVisibility(DocumentVisibility.PUBLIC);

        UserPlanResolverService.UserPlanLimits mockLimits = new UserPlanResolverService.UserPlanLimits("FREE", 100, 100, false);
        when(systemConfigRepository.findById("feature.upload.enabled")).thenReturn(Optional.empty());
        when(userPlanResolverService.resolveLimits(userId)).thenReturn(mockLimits);
        when(documentRepository.countByUserIdAndDeletedAtIsNull(userId)).thenReturn(0L);

        Map<String, String> cloudinaryResult = Map.of("url", "http://cloudinary.com/test.txt", "public_id", "test");
        when(cloudinaryService.upload(any())).thenReturn(cloudinaryResult);

        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document saved = inv.getArgument(0);
            saved.setId(docId);
            return saved;
        });

        DocumentResponse expectedResponse = DocumentResponse.builder()
                .id(docId)
                .visibility(DocumentVisibility.PUBLIC)
                .uploadStatus(UploadStatus.PROCESSING)
                .build();
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            DocumentResponse response = documentService.upload(file, request, userId);
            assertNotNull(response);
            assertEquals(DocumentVisibility.PUBLIC, response.getVisibility());
            verify(documentRepository).save(argThat(doc -> doc.getApprovalStatus() == ApprovalStatus.PENDING));
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void approveDocument_ShouldSetApprovedAndDmcaVerified() {
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(moderationRepository.save(any(Moderation.class))).thenAnswer(inv -> inv.getArgument(0));

        Moderation moderation = moderationService.approveDocument(docId, moderator.getId(), moderator, pendingDoc);

        assertNotNull(moderation);
        assertEquals(ApprovalStatus.APPROVED, pendingDoc.getApprovalStatus());
        assertTrue(pendingDoc.getDmcaVerified());
        assertNotNull(pendingDoc.getDmcaVerifiedAt());
        assertEquals(moderator.getId(), pendingDoc.getDmcaVerifiedBy());
        verify(documentRepository).save(pendingDoc);
    }

    @Test
    void rejectDocument_ShouldSetRejectedAndReason() {
        String reason = "Violates copyright guidelines";
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(moderationRepository.save(any(Moderation.class))).thenAnswer(inv -> inv.getArgument(0));

        Moderation moderation = moderationService.rejectDocument(docId, moderator.getId(), moderator, pendingDoc, reason);

        assertNotNull(moderation);
        assertEquals(ApprovalStatus.REJECTED, pendingDoc.getApprovalStatus());
        assertEquals(reason, pendingDoc.getRejectionReason());
        assertNotNull(pendingDoc.getDmcaVerifiedAt());
        assertEquals(moderator.getId(), pendingDoc.getDmcaVerifiedBy());
        verify(documentRepository).save(pendingDoc);
    }

    @Test
    void takedownDocument_ShouldSetTakedownAndDeleteChunks() {
        Document approvedDoc = Document.builder()
                .id(docId)
                .userId(userId)
                .title("Approved Doc")
                .visibility(DocumentVisibility.PUBLIC)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(moderationRepository.save(any(Moderation.class))).thenAnswer(inv -> inv.getArgument(0));

        Moderation moderation = moderationService.takedownDocument(docId, moderator.getId(), moderator, approvedDoc);

        assertNotNull(moderation);
        assertEquals(ApprovalStatus.DMCA_TAKEN_DOWN, approvedDoc.getApprovalStatus());
        verify(documentProcessor, times(1)).deleteChunks(docId);
        assertNotNull(approvedDoc.getDmcaVerifiedAt());
        assertEquals(moderator.getId(), approvedDoc.getDmcaVerifiedBy());
        verify(documentRepository).save(approvedDoc);
    }
}
