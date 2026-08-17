package com.example.swp391.aistudenthub.feature.moderator.service;

import java.util.UUID;

/** Performs AI content moderation for publicly visible documents. */
public interface ContentScanService {
    void scanAndAutoTakedownIfViolating(UUID documentId);
}
