package io.github.dealmicroservice.itegration.RabbitMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.config.RabbitMQConfig;
import io.github.dealmicroservice.model.dto.ContractorMessageDTO;
import io.github.dealmicroservice.model.entity.InboxEvent;
import io.github.dealmicroservice.repository.InboxEventRepository;
import io.github.dealmicroservice.service.DealContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest",
        "dealmicroservice.rabbitmq.maxRetries=3"
})
class ContractorMessageIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InboxEventRepository inboxEventRepository;

    @MockitoBean
    private DealContractorService dealContractorService;

    @BeforeEach
    void setUp() {
        inboxEventRepository.deleteAll();
        reset(dealContractorService);
    }

    /**
     * Тест успешной обработки сообщения контрагента
     */
    @Test
    void processContractor_ProcessContractorUpdateSuccessfully() throws Exception {
        String messageId = UUID.randomUUID().toString();
        ContractorMessageDTO contractor = ContractorMessageDTO.builder()
                .id("test-contractor")
                .inn("1234567890")
                .name("Test Contractor")
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();

        String payload = objectMapper.writeValueAsString(contractor);
        Message message = MessageBuilder.withBody(payload.getBytes())
                .setMessageId(messageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(messageId);
            assertTrue(inboxEvent.isPresent());
            assertTrue(inboxEvent.get().getProcessed());
            assertEquals("test-contractor", inboxEvent.get().getAggregateId());
            assertEquals("UPDATED", inboxEvent.get().getEventType());
            assertEquals(1L, inboxEvent.get().getVersion());
        });
    }

    /**
     * Тест обработки дублированного сообщения
     */
    @Test
    void processDuplicateMessage_IgnoreDuplicateMessage() throws Exception {
        String messageId = UUID.randomUUID().toString();
        ContractorMessageDTO contractor = ContractorMessageDTO.builder()
                .id("test-contractor")
                .inn("1234567890")
                .name("Test Contractor")
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();

        String payload = objectMapper.writeValueAsString(contractor);
        Message message = MessageBuilder.withBody(payload.getBytes())
                .setMessageId(messageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dealContractorService, times(1)).updateContractorInDeals(any(ContractorMessageDTO.class));
        });

        reset(dealContractorService);

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, message);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dealContractorService, never()).updateContractorInDeals(any(ContractorMessageDTO.class));

            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(messageId);
            assertTrue(inboxEvent.isPresent());
            assertTrue(inboxEvent.get().getProcessed());
            assertEquals(0, inboxEvent.get().getRetryCount());
        });
    }

    /**
     * Тест retry-логики при ошибке обработки
     */
    @Test
    void retryTest_RetryOnProcessingError() throws Exception {
        String messageId = UUID.randomUUID().toString();
        ContractorMessageDTO contractor = ContractorMessageDTO.builder()
                .id("test-contractor")
                .inn("1234567890")
                .name("Test Contractor")
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();

        String payload = objectMapper.writeValueAsString(contractor);
        Message message = MessageBuilder.withBody(payload.getBytes())
                .setMessageId(messageId)
                .setContentType("text/plain")
                .build();

        doThrow(new RuntimeException("Database connection error"))
                .doNothing()
                .when(dealContractorService).updateContractorInDeals(any(ContractorMessageDTO.class));

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, message);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(messageId);
                    assertTrue(inboxEvent.isPresent());

                    assertTrue(inboxEvent.get().getRetryCount() > 0);
                    assertNotNull(inboxEvent.get().getErrorMessage());
                    assertTrue(inboxEvent.get().getErrorMessage().contains("Database connection error"));
                });
    }

    /**
     * Тест превышения максимального количества retry
     */
    @Test
    void testMaxRetriesCount_StopRetryingAfterMaxValue() throws Exception {
        String messageId = UUID.randomUUID().toString();
        ContractorMessageDTO contractor = ContractorMessageDTO.builder()
                .id("test-contractor")
                .inn("1234567890")
                .name("Test Contractor")
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();

        String payload = objectMapper.writeValueAsString(contractor);
        Message message = MessageBuilder.withBody(payload.getBytes())
                .setMessageId(messageId)
                .setContentType("text/plain")
                .build();

        doThrow(new RuntimeException("Persistent error"))
                .when(dealContractorService).updateContractorInDeals(any(ContractorMessageDTO.class));

        InboxEvent existingEvent = InboxEvent.builder()
                .messageId(messageId)
                .aggregateId(contractor.getId())
                .aggregateType("Contractor")
                .eventType("UPDATED")
                .payload(payload)
                .retryCount(3)
                .processed(false)
                .version(1L)
                .build();

        inboxEventRepository.save(existingEvent);

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(messageId);
            assertTrue(inboxEvent.isPresent());

            assertTrue(inboxEvent.get().getProcessed());
            assertEquals(3, inboxEvent.get().getRetryCount());
        });
    }

    /**
     * Тест обработки сообщений с разными версиями (актуальность по version)
     */
    @Test
    void VersionTest_IgnoreOlderVersionMessage() throws Exception {
        String contractorId = "test-contractor";

        String newMessageId = UUID.randomUUID().toString();
        ContractorMessageDTO newerContractor = ContractorMessageDTO.builder()
                .id(contractorId)
                .inn("1234567890")
                .name("Updated Contractor")
                .modifyDate(LocalDateTime.now())
                .version(2L)
                .build();

        String newPayload = objectMapper.writeValueAsString(newerContractor);
        Message newMessage = MessageBuilder.withBody(newPayload.getBytes())
                .setMessageId(newMessageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, newMessage);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(newMessageId);
            assertTrue(inboxEvent.isPresent() && inboxEvent.get().getProcessed());
            assertEquals(2L, inboxEvent.get().getVersion());
        });

        reset(dealContractorService);

        String oldMessageId = UUID.randomUUID().toString();
        ContractorMessageDTO olderContractor = ContractorMessageDTO.builder()
                .id(contractorId)
                .inn("1234567890")
                .name("Old Contractor")
                .modifyDate(LocalDateTime.now().plusHours(1))
                .version(1L)
                .build();

        String oldPayload = objectMapper.writeValueAsString(olderContractor);
        Message oldMessage = MessageBuilder.withBody(oldPayload.getBytes())
                .setMessageId(oldMessageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, oldMessage);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dealContractorService, never()).updateContractorInDeals(any(ContractorMessageDTO.class));

            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(oldMessageId);
            assertTrue(inboxEvent.isPresent());
            assertTrue(inboxEvent.get().getProcessed());
            assertEquals(1L, inboxEvent.get().getVersion());
        });
    }

    /**
     * Тест обработки сообщения с более новой версией
     */
    @Test
    void VersionTest_ProcessNewerVersionMessage() throws Exception {
        String contractorId = "test-contractor";

        String oldMessageId = UUID.randomUUID().toString();
        ContractorMessageDTO olderContractor = ContractorMessageDTO.builder()
                .id(contractorId)
                .inn("1234567890")
                .name("Old Contractor")
                .modifyDate(LocalDateTime.now())
                .version(1L)
                .build();

        String oldPayload = objectMapper.writeValueAsString(olderContractor);
        Message oldMessage = MessageBuilder.withBody(oldPayload.getBytes())
                .setMessageId(oldMessageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, oldMessage);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(oldMessageId);
            assertTrue(inboxEvent.isPresent() && inboxEvent.get().getProcessed());
            verify(dealContractorService, times(1)).updateContractorInDeals(any(ContractorMessageDTO.class));
        });

        reset(dealContractorService);

        String newMessageId = UUID.randomUUID().toString();
        ContractorMessageDTO newerContractor = ContractorMessageDTO.builder()
                .id(contractorId)
                .inn("1234567890")
                .name("Updated Contractor")
                .modifyDate(LocalDateTime.now().minusHours(1))
                .version(3L)
                .build();

        String newPayload = objectMapper.writeValueAsString(newerContractor);
        Message newMessage = MessageBuilder.withBody(newPayload.getBytes())
                .setMessageId(newMessageId)
                .setContentType("text/plain")
                .build();

        rabbitTemplate.send(RabbitMQConfig.DEALS_CONTRACTOR_QUEUE, newMessage);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(dealContractorService, times(1)).updateContractorInDeals(any(ContractorMessageDTO.class));

            Optional<InboxEvent> inboxEvent = inboxEventRepository.findInboxEventByMessageId(newMessageId);
            assertTrue(inboxEvent.isPresent());
            assertTrue(inboxEvent.get().getProcessed());
            assertEquals(3L, inboxEvent.get().getVersion());
        });
    }

}