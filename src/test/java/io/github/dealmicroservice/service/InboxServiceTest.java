package io.github.dealmicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.model.entity.InboxEvent;
import io.github.dealmicroservice.repository.InboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxServiceTest {

    @Mock
    private InboxEventRepository inboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InboxService inboxService;

    private InboxEvent testEvent;
    private String testPayload;

    @BeforeEach
    void setUp() {

        testPayload = "{\"id\":\"123\",\"name\":\"Test\",\"version\":1}";
        testEvent = InboxEvent.builder()
                .id(1L)
                .messageId("test-msg-id")
                .aggregateId("test-aggr-id")
                .aggregateType("Contractor")
                .eventType("UPDATED")
                .payload(testPayload)
                .processed(false)
                .retryCount(0)
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();
    }

    @Test
    void saveInboxEvent_Success() throws JsonProcessingException {
        String messageId = "test-msg-id";
        String aggregateId = "test-aggr-id";
        String aggregateType = "Contractor";
        String eventType = "UPDATED";
        Object payload = new Object();
        Long version = 1L;

        when(objectMapper.writeValueAsString(payload)).thenReturn(testPayload);
        when(inboxEventRepository.save(any(InboxEvent.class))).thenReturn(testEvent);

        Long result = inboxService.saveInboxEvent(messageId, aggregateId, aggregateType, eventType, payload, version);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(objectMapper).writeValueAsString(payload);
        verify(inboxEventRepository).save(any(InboxEvent.class));
    }

    @Test
    void incrementRetry_Success() {
        Long eventId = 1L;
        String errorMessage = "Test error";
        testEvent.setRetryCount(1);

        when(inboxEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        inboxService.incrementRetry(eventId, errorMessage);

        verify(inboxEventRepository).updateRetryInfo(eq(eventId), eq(2), eq(errorMessage), any(LocalDateTime.class));
    }

    @Test
    void incrementRetry_NullEventId() {
        inboxService.incrementRetry(null, "error");

        verify(inboxEventRepository, never()).findById(anyLong());
        verify(inboxEventRepository, never()).updateRetryInfo(anyLong(), anyInt(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void getInboxEventByMessageId_Found() {
        String messageId = "test-msg-id";
        when(inboxEventRepository.findInboxEventByMessageId(messageId)).thenReturn(Optional.of(testEvent));

        Optional<InboxEvent> result = inboxService.getInboxEventByMessageId(messageId);

        assertTrue(result.isPresent());
        assertEquals(testEvent, result.get());
    }

    @Test
    void getInboxEventByMessageId_NotFound() {
        String messageId = "test-msg-id";
        when(inboxEventRepository.findInboxEventByMessageId(messageId)).thenReturn(Optional.empty());

        Optional<InboxEvent> result = inboxService.getInboxEventByMessageId(messageId);

        assertFalse(result.isPresent());
    }

    @Test
    void isMessageMoreRecent_NoProcessedEvents() {
        String aggregateId = "test-aggr-id";
        Long messageVersion = 1L;

        when(inboxEventRepository.findMaxProcessedVersionByAggregateId(aggregateId))
                .thenReturn(Optional.empty());

        boolean result = inboxService.isMessageMoreRecent(aggregateId, messageVersion);

        assertTrue(result);
    }

    @Test
    void isMessageMoreRecent_MessageVersionIsNewer() {
        String aggregateId = "test-aggr-id";
        Long messageVersion = 3L;
        Long maxProcessedVersion = 2L;

        when(inboxEventRepository.findMaxProcessedVersionByAggregateId(aggregateId))
                .thenReturn(Optional.of(maxProcessedVersion));

        boolean result = inboxService.isMessageMoreRecent(aggregateId, messageVersion);

        assertTrue(result);
    }

    @Test
    void isMessageMoreRecent_MessageVersionIsOlder() {
        String aggregateId = "test-aggr-id";
        Long messageVersion = 1L;
        Long maxProcessedVersion = 3L;

        when(inboxEventRepository.findMaxProcessedVersionByAggregateId(aggregateId))
                .thenReturn(Optional.of(maxProcessedVersion));

        boolean result = inboxService.isMessageMoreRecent(aggregateId, messageVersion);

        assertFalse(result);
    }

    @Test
    void isMessageMoreRecent_MessageVersionIsSame() {
        String aggregateId = "test-aggr-id";
        Long messageVersion = 2L;
        Long maxProcessedVersion = 2L;

        when(inboxEventRepository.findMaxProcessedVersionByAggregateId(aggregateId))
                .thenReturn(Optional.of(maxProcessedVersion));

        boolean result = inboxService.isMessageMoreRecent(aggregateId, messageVersion);

        assertFalse(result);
    }

    @Test
    void isMessageMoreRecent_NullMessageVersion() {
        String aggregateId = "test-aggr-id";

        boolean result = inboxService.isMessageMoreRecent(aggregateId, null);

        assertTrue(result);
        verify(inboxEventRepository, never()).findMaxProcessedVersionByAggregateId(anyString());
    }

}