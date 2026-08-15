package com.example.swp391.aistudenthub.feature.chat.service;

import com.example.swp391.aistudenthub.exception.AppException;
import com.example.swp391.aistudenthub.exception.ErrorCode;
import com.example.swp391.aistudenthub.feature.auth.entity.User;
import com.example.swp391.aistudenthub.feature.auth.repository.UserRepository;
import com.example.swp391.aistudenthub.feature.chat.dto.response.ChatQuotaResponse;
import com.example.swp391.aistudenthub.feature.chat.repository.ChatMessageRepository;
import com.example.swp391.aistudenthub.feature.admin.repository.SystemConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQuotaServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private com.example.swp391.aistudenthub.feature.payment.service.UserPlanResolverService userPlanResolverService;

    @InjectMocks
    private AiQuotaService aiQuotaService;

    @Test
    void reserveQuestion_returnsRemainingQuota() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(systemConfigRepository.findById(AiQuotaService.DAILY_LIMIT_CONFIG_KEY)).thenReturn(Optional.empty());
        when(chatMessageRepository.countUserQuestionsSince(any(), any())).thenReturn(3L);
        when(userPlanResolverService.resolveLimits(any())).thenReturn(
                new com.example.swp391.aistudenthub.feature.payment.service.UserPlanResolverService.UserPlanLimits("Cơ bản", 20, 50, false)
        );

        ChatQuotaResponse response = aiQuotaService.reserveQuestion(userId);

        assertEquals(20, response.getDailyLimit());
        assertEquals(3, response.getUsed());
        assertEquals(17, response.getRemaining());
    }

    @Test
    void reserveQuestion_throwsWhenDailyLimitIsReached() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(systemConfigRepository.findById(AiQuotaService.DAILY_LIMIT_CONFIG_KEY)).thenReturn(Optional.empty());
        when(chatMessageRepository.countUserQuestionsSince(any(), any())).thenReturn(20L);
        when(userPlanResolverService.resolveLimits(any())).thenReturn(
                new com.example.swp391.aistudenthub.feature.payment.service.UserPlanResolverService.UserPlanLimits("Cơ bản", 20, 50, false)
        );

        AppException exception = assertThrows(AppException.class, () -> aiQuotaService.reserveQuestion(userId));

        assertEquals(ErrorCode.LIMIT_EXCEEDED, exception.getErrorCode());
    }
}
