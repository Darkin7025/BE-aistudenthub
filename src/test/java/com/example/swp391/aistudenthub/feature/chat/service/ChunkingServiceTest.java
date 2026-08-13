package com.example.swp391.aistudenthub.feature.chat.service;

import com.example.swp391.aistudenthub.feature.chat.dto.TextChunk;
import com.example.swp391.aistudenthub.feature.chat.enums.ChunkingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingServiceImpl();

    @Test
    void testChunkByHeadlineRoman_SplitsAtRomanNumeralsAndSubcategories() {
        String docText = "I. Giới thiệu chung\n" +
                "Nội dung giới thiệu 1.\n" +
                "Nội dung giới thiệu 2.\n" +
                "a. Lịch sử hình thành\n" +
                "Lịch sử hình thành 1.\n" +
                "Lịch sử hình thành 2.\n" +
                "b) Thành tựu\n" +
                "Thành tựu 1.\n" +
                "II. Nội dung chính\n" +
                "Nội dung chính 1.\n" +
                "Nội dung chính 2.";

        List<TextChunk> chunks = chunkingService.chunkText(docText, ChunkingStrategy.HEADLINE_ROMAN);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        
        // Print out content to verify
        for (TextChunk tc : chunks) {
            System.out.println("Chunk " + tc.getChunkIndex() + ":\n" + tc.getContent());
            System.out.println("--------------------");
        }

        // We expect exactly 4 chunks because of boundaries:
        // Chunk 0: "I. Giới thiệu chung\nNội dung giới thiệu 1.\nNội dung giới thiệu 2."
        // Chunk 1: "a. Lịch sử hình thành\nLịch sử hình thành 1.\nLịch sử hình thành 2."
        // Chunk 2: "b) Thành tựu\nThành tựu 1."
        // Chunk 3: "II. Nội dung chính\nNội dung chính 1.\nNội dung chính 2."
        assertTrue(chunks.size() >= 4);
        
        // Assert chunk contents contain the specific headers
        assertTrue(chunks.get(0).getContent().contains("I. Giới thiệu chung"));
        assertTrue(chunks.get(1).getContent().contains("a. Lịch sử hình thành"));
        assertTrue(chunks.get(2).getContent().contains("b) Thành tựu"));
        assertTrue(chunks.get(3).getContent().contains("II. Nội dung chính"));
    }
}
