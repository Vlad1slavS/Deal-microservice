package io.github.dealmicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.model.entity.InboxEvent;
import io.github.dealmicroservice.repository.InboxEventRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InboxService {

    private final Logger log = LogManager.getLogger(InboxService.class);

    private final InboxEventRepository inboxEventRepository;
    private final ObjectMapper objectMapper;

    public InboxService(InboxEventRepository inboxEventRepository, ObjectMapper objectMapper) {
        this.inboxEventRepository = inboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void incrementRetry(Long eventId, String errorMessage) {
        if (eventId != null) {
            InboxEvent event = inboxEventRepository.findById(eventId).orElse(null);
            if (event != null) {
                int newCount = event.getRetryCount() + 1;
                inboxEventRepository.updateRetryInfo(eventId, newCount, errorMessage, LocalDateTime.now());
            }
        }
    }

    public Optional<InboxEvent> getInboxEventByMessageId(String messageId) {
        return  inboxEventRepository.findInboxEventByMessageId(messageId);
    }

    @Transactional
    public Long saveInboxEvent(String messageId, String aggregateId, String aggregateType,
                               String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            InboxEvent event = InboxEvent.builder()
                    .messageId(messageId)
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .modifyDate(LocalDateTime.now())
                    .build();

            InboxEvent savedEvent = inboxEventRepository.save(event);

            log.debug("Inbox event saved: messageId={}, aggregateId={}, id={}", messageId, aggregateId, savedEvent.getId());

            return savedEvent.getId();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for inbox event", e);
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }

    @Transactional
    public void markAsProcessed(Long eventId) {
        if (eventId != null) {
            inboxEventRepository.markAsProcessed(eventId, LocalDateTime.now());
        }
    }

    /**
     * Проверяет, является ли сообщение более актуальным чем уже обработанные
     */
    public boolean isMessageMoreRecent(String aggregateId, LocalDateTime messageModifyDate) {

        if (messageModifyDate == null) {
            log.warn("Message modify date is null for aggregateId: {}", aggregateId);
            return true;
        }

        Optional<InboxEvent> latestProcessed = inboxEventRepository.findLatestProcessedEventByAggregateId(aggregateId);

        if (latestProcessed.isEmpty()) {
            log.debug("No processed events found for aggregateId: {}, treating as recent", aggregateId);
            return true;
        }

        LocalDateTime lastProcessedModifyDate = latestProcessed.get().getModifyDate();
        boolean isMoreRecent = messageModifyDate.isAfter(lastProcessedModifyDate);

        log.debug("Message actuality check for aggregateId: {}, messageDate: {}, lastProcessedDate: {}, isMoreRecent: {}",
                aggregateId, messageModifyDate, lastProcessedModifyDate, isMoreRecent);

        return isMoreRecent;
    }

}
