package io.github.dealmicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.config.RabbitMQConfig;
import io.github.dealmicroservice.model.dto.ContractorMessageDTO;
import io.github.dealmicroservice.model.entity.InboxEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Принимает входящие сообщения и обрабатывает их
 */
@Component
public class ContractorMessageListener {

    @Value("${dealmicroservice.rabbitmq.maxRetries:3}")
    private int maxRetries;

    private final Logger log = LogManager.getLogger(ContractorMessageListener.class);

    private final DealContractorService dealContractorService;
    private final InboxService inboxService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public ContractorMessageListener(DealContractorService dealContractorService,
                                     InboxService inboxService,
                                     ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.dealContractorService = dealContractorService;
        this.inboxService = inboxService;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.DEALS_CONTRACTOR_QUEUE)
    public void handleContractorUpdate(String messagePayload, Message message) {

        String messageId = message.getMessageProperties().getMessageId();

        if (messageId == null) {
            log.error("No messageId in message, rejecting");
            throw new IllegalArgumentException("Message must have messageId");
        }

        log.debug("Received contractor update message: messageId={}", messageId);

        Optional<InboxEvent> existingEvent = inboxService.getInboxEventByMessageId(messageId);
        Long inboxEventId = null;
        ContractorMessageDTO contractorMessage = null;

        try {
            if (existingEvent.isPresent()) {
                InboxEvent event = existingEvent.get();
                if (event.getProcessed()) {
                    log.debug("Duplicate message ignored: messageId={}", messageId);
                    return;
                }
                if (event.getRetryCount() >= maxRetries) {
                    log.error("Max retries exceeded for messageId={}", messageId);
                    sendToFinalDLQ(messagePayload, message, event);
                    inboxService.markAsProcessed(event.getId());
                    return;
                }
                inboxEventId = event.getId();
                contractorMessage = objectMapper.readValue(event.getPayload(), ContractorMessageDTO.class);
                inboxService.incrementRetry(inboxEventId, "Retrying unprocessed event");

            } else {
                contractorMessage = objectMapper.readValue(messagePayload, ContractorMessageDTO.class);
                inboxEventId = inboxService.saveInboxEvent(
                        messageId,
                        contractorMessage.getId(),
                        "Contractor",
                        "UPDATED",
                        contractorMessage,
                        contractorMessage.getVersion()
                );
            }


            if (!inboxService.isMessageMoreRecent(contractorMessage.getId(), contractorMessage.getVersion())) {
                log.debug("Message is not more recent than already processed: contractorId={}, messageVersion={}",
                        contractorMessage.getId(), contractorMessage.getVersion());

                inboxService.markAsProcessed(inboxEventId);
                return;
            }

            dealContractorService.updateContractorInDeals(contractorMessage);

            inboxService.markAsProcessed(inboxEventId);

            log.info("Contractor update processed successfully: contractorId={}, messageId={}",
                    contractorMessage.getId(), messageId);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse contractor message: messageId={}", messageId, e);

            if (inboxEventId != null) {
                sendToRetryOrFinalQueue(messagePayload, message, inboxEventId, "JSON parsing error: " + e.getMessage());
            }
            throw new RuntimeException("Failed to parse message", e);

        } catch (Exception e) {
            log.error("Failed to process contractor update: messageId={}, contractorId={}",
                    messageId, contractorMessage != null ? contractorMessage.getId() : "unknown", e);

            if (inboxEventId != null) {
                sendToRetryOrFinalQueue(messagePayload, message, inboxEventId, e.getMessage());
            }
            throw new RuntimeException("Failed to process contractor update", e);
        }
    }

    private void sendToFinalDLQ(String messagePayload, Message originalMessage, InboxEvent event) {
        try {
            MessageProperties properties = new MessageProperties();

            if (originalMessage.getMessageProperties().getHeaders() != null) {
                properties.getHeaders().putAll(originalMessage.getMessageProperties().getHeaders());
            }

            properties.getHeaders().put("x-final-dlq-reason", "max-retries-exceeded");
            properties.getHeaders().put("x-retry-count", event.getRetryCount());
            properties.getHeaders().put("x-original-queue", RabbitMQConfig.DEALS_CONTRACTOR_QUEUE);
            properties.getHeaders().put("x-inbox-event-id", event.getId());

            properties.setMessageId(originalMessage.getMessageProperties().getMessageId());

            Message finalMessage = new Message(messagePayload.getBytes(), properties);

            rabbitTemplate.send(
                    RabbitMQConfig.DEAL_FINAL_DEAD_EXCHANGE,
                    RabbitMQConfig.FINAL_DEAD_ROUTING_KEY,
                    finalMessage
            );

            log.info("Message sent to final DLQ: messageId={}, retryCount={}",
                    originalMessage.getMessageProperties().getMessageId(), event.getRetryCount());

        } catch (Exception e) {
            log.error("Failed to send message to final DLQ: messageId={}",
                    originalMessage.getMessageProperties().getMessageId(), e);
        }
    }

    private void sendToRetryOrFinalQueue(String messagePayload, Message originalMessage,
                                       Long inboxEventId, String errorMessage) {

        Optional<InboxEvent> eventOpt = inboxService.getInboxEventById(inboxEventId);
        if (eventOpt.isPresent()) {
            InboxEvent event = eventOpt.get();

            if (event.getRetryCount() >= maxRetries) {
                log.warn("Max retries reached for messageId={}, sending to final DLQ",
                        originalMessage.getMessageProperties().getMessageId());
                sendToFinalDLQ(messagePayload, originalMessage, event);
                inboxService.markAsProcessed(inboxEventId);
            } else {
                inboxService.incrementRetry(inboxEventId, errorMessage);
            }
        }
    }

}
