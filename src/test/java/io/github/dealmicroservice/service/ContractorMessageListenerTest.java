package io.github.dealmicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.model.dto.ContractorMessageDTO;
import io.github.dealmicroservice.model.entity.InboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractorMessageListenerTest {

    @Mock
    private DealContractorService dealContractorService;

    @Mock
    private InboxService inboxService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private ContractorMessageListener contractorMessageListener;

    private ContractorMessageDTO contractorMessage;
    private String messagePayload;
    private String messageId;
    private InboxEvent inboxEvent;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(contractorMessageListener, "maxRetries", 3);

        messageId = "test-msg-id";
        contractorMessage = ContractorMessageDTO.builder()
                .id("test-contractor")
                .inn("1234567890")
                .name("Test Contractor")
                .modifyDate(LocalDateTime.now())
                .build();

        messagePayload = "{\"id\":\"test-contractor\",\"inn\":\"1234567890\",\"name\":\"Test Contractor\"}";

        inboxEvent = InboxEvent.builder()
                .id(1L)
                .messageId(messageId)
                .aggregateId("test-contractor")
                .processed(false)
                .retryCount(0)
                .payload(messagePayload)
                .build();

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getMessageId()).thenReturn(messageId);
    }

    /**
     * Тест успешной обработки нового сообщения.
     */
    @Test
    void ContractorUpdateNewMessage_Success() throws JsonProcessingException {

        when(inboxService.getInboxEventByMessageId(messageId)).thenReturn(Optional.empty());
        when(objectMapper.readValue(messagePayload, ContractorMessageDTO.class)).thenReturn(contractorMessage);
        when(inboxService.saveInboxEvent(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(1L);
        when(inboxService.isMessageMoreRecent(anyString(), any(LocalDateTime.class))).thenReturn(true);

        contractorMessageListener.handleContractorUpdate(messagePayload, message);

        verify(inboxService).saveInboxEvent(messageId, "test-contractor", "Contractor", "UPDATED", contractorMessage);
        verify(dealContractorService).updateContractorInDeals(contractorMessage);
        verify(inboxService).markAsProcessed(1L);
    }

    /**
     * Тест игнорирования дублированного сообщения.
     */
    @Test
    void ContractorUpdateDuplicateMessage_Ignored() {

        inboxEvent.setProcessed(true);
        when(inboxService.getInboxEventByMessageId(messageId)).thenReturn(Optional.of(inboxEvent));

        contractorMessageListener.handleContractorUpdate(messagePayload, message);

        verify(dealContractorService, never()).updateContractorInDeals(any());
        verify(inboxService, never()).markAsProcessed(anyLong());
        verify(inboxService, never()).incrementRetry(anyLong(), anyString());
    }

    /**
     * Тест обработки сообщения с превышением максимального количества попыток.
     */
    @Test
    void ContractorUpdate_MaxRetriesExceeded() {

        inboxEvent.setRetryCount(3);
        when(inboxService.getInboxEventByMessageId(messageId)).thenReturn(Optional.of(inboxEvent));

        contractorMessageListener.handleContractorUpdate(messagePayload, message);

        verify(inboxService).markAsProcessed(1L);
        verify(dealContractorService, never()).updateContractorInDeals(any());
    }

    /**
     * Тест игнорирования устаревшего сообщения.
     */
    @Test
    void ContractorUpdate_MessageNotMoreRecent() throws JsonProcessingException {

        when(inboxService.getInboxEventByMessageId(messageId)).thenReturn(Optional.empty());
        when(objectMapper.readValue(messagePayload, ContractorMessageDTO.class)).thenReturn(contractorMessage);
        when(inboxService.saveInboxEvent(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(1L);
        when(inboxService.isMessageMoreRecent(anyString(), any(LocalDateTime.class))).thenReturn(false);

        contractorMessageListener.handleContractorUpdate(messagePayload, message);

        verify(inboxService).markAsProcessed(1L);
        verify(dealContractorService, never()).updateContractorInDeals(any());
    }

    /**
     * Тест обработки сообщения без messageId.
     */
    @Test
    void handleContractorUpdate_NoMessageId() {

        when(messageProperties.getMessageId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                contractorMessageListener.handleContractorUpdate(messagePayload, message));

        verify(inboxService, never()).getInboxEventByMessageId(anyString());
    }

}
