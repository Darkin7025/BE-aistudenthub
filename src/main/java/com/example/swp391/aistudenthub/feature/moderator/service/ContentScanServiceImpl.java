package com.example.swp391.aistudenthub.feature.moderator.service;

import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.chat.repository.DocumentChunkRepository;
import com.example.swp391.aistudenthub.feature.chat.service.AIService;
import com.example.swp391.aistudenthub.feature.document.entity.Document;
import com.example.swp391.aistudenthub.feature.document.enums.ApprovalStatus;
import com.example.swp391.aistudenthub.feature.document.enums.DocumentVisibility;
import com.example.swp391.aistudenthub.feature.document.repository.DocumentRepository;
import com.example.swp391.aistudenthub.feature.moderator.entity.Moderation;
import com.example.swp391.aistudenthub.feature.moderator.repository.ModerationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentScanServiceImpl implements ContentScanService {

    private static final int MAX_SCAN_CHARACTERS = 8_000;
    private static final String SYSTEM_MODERATOR_EMAIL = "admin@aistudyhub.com";

    private final DocumentRepository documentRepository;
    private final AIService aiService;
    private final DocumentChunkRepository documentChunkRepository;
    private final ModerationRepository moderationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void scanAndAutoTakedownIfViolating(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null || document.getVisibility() != DocumentVisibility.PUBLIC) {
            return;
        }

        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            log.info("Skipping AI content scan for document {} because it has no extracted text", documentId);
            return;
        }

        String textToScan = extractedText.substring(0, Math.min(extractedText.length(), MAX_SCAN_CHARACTERS));
        ScanResult result;
        try {
            result = parseScanResult(aiService.generateAnswer(buildPrompt(textToScan)));
        } catch (Exception e) {
            // A failed or malformed AI response must never result in an automatic takedown.
            log.warn("Unable to parse AI content scan result for document {}. Leaving it pending.", documentId, e);
            return;
        }

        if (!result.violated()) {
            log.info("AI content scan found no violation for document {}", documentId);
            return;
        }

        User systemModerator = userRepository.findByEmailAndDeletedAtIsNull(SYSTEM_MODERATOR_EMAIL).orElse(null);
        if (systemModerator == null) {
            log.error("Cannot auto-take down document {} because system moderator {} is missing",
                    documentId, SYSTEM_MODERATOR_EMAIL);
            return;
        }

        document.setApprovalStatus(ApprovalStatus.DMCA_TAKEN_DOWN);
        documentChunkRepository.deleteByDocumentId(documentId);
        moderationRepository.save(Moderation.builder()
                .document(document)
                .moderator(systemModerator)
                .action(Moderation.ModerationAction.REJECTED)
                .reason("[AUTO-AI] " + result.reason())
                .build());

        log.warn("Document {} was automatically taken down after AI content scan: {}", documentId, result.reason());
    }

    private ScanResult parseScanResult(String response) throws Exception {
        String json = stripMarkdownCodeFence(response);
        JsonNode root = objectMapper.readTree(json);
        JsonNode violatedNode = root.get("violated");
        if (violatedNode == null || !violatedNode.isBoolean()) {
            throw new IllegalArgumentException("Missing boolean 'violated' field");
        }

        JsonNode reasonNode = root.get("reason");
        String reason = reasonNode != null && reasonNode.isTextual() ? reasonNode.asText().trim() : "No reason supplied";
        return new ScanResult(violatedNode.booleanValue(), reason.isEmpty() ? "No reason supplied" : reason);
    }

    private String stripMarkdownCodeFence(String response) {
        if (response == null) {
            throw new IllegalArgumentException("AI response is null");
        }
        String value = response.trim();
        if (value.startsWith("```")) {
            int firstNewline = value.indexOf('\n');
            int closingFence = value.lastIndexOf("```");
            if (firstNewline < 0 || closingFence <= firstNewline) {
                throw new IllegalArgumentException("Invalid markdown code fence");
            }
            value = value.substring(firstNewline + 1, closingFence).trim();
        }
        return value;
    }

    private String buildPrompt(String text) {
        return """
                You are a content moderation classifier for an academic document platform.
                Decide whether the document violates community rules because it contains any of:
                1. sexually explicit or pornographic content;
                2. incitement to violence;
                3. sensitive personal information, such as national ID numbers or bank account details;
                4. malware or instructions for cyber attacks;
                5. spam or advertising unrelated to academics.

                Treat legitimate academic discussion of these topics as non-violating unless the content itself is clearly harmful, explicit, or actionable. Return only valid JSON, without markdown or extra text, in this exact shape:
                {"violated": true, "reason": "brief explanation"}

                Document content:
                ---
                %s
                ---
                """.formatted(text);
    }

    private record ScanResult(boolean violated, String reason) {
    }
}
