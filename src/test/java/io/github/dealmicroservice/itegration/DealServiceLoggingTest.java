package io.github.dealmicroservice.itegration;

import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.service.DealService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Простой тест для логов в консоль
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "audit.outputs=console"
})
@ExtendWith(OutputCaptureExtension.class)
public class DealServiceLoggingTest {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("deal_db")
            .withUsername("deal")
            .withPassword("1234");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private DealService dealService;

    private static class TestAppender extends AbstractAppender {
        private final List<String> messages = new ArrayList<>();

        public TestAppender(String name) {
            super(name, null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        public List<String> getMessages() {
            return new ArrayList<>(messages);
        }

        public void clear() {
            messages.clear();
        }
    }

    private TestAppender testAppender;
    private Logger rootLogger;

    @BeforeEach
    void setUp() {
        testAppender = new TestAppender("TestAppender");
        testAppender.start();

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

        rootLogger = (Logger) LogManager.getRootLogger();

        Logger audit_method_console_logger = loggerContext.getLogger("AUDIT_METHOD_CONSOLE");
        audit_method_console_logger.addAppender(testAppender);
        audit_method_console_logger.setLevel(Level.DEBUG);

        rootLogger.addAppender(testAppender);

        rootLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        if (rootLogger != null && testAppender != null) {
            rootLogger.removeAppender(testAppender);
        }
    }

    @Test
    public void dealServiceGetDealById_LogMethodToConsole(CapturedOutput output) {

        UUID id = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class, () -> {
            dealService.getDealById(id);
        });

        List<String> logMessages = testAppender.getMessages();
        
        assertThat(logMessages).anyMatch(msg -> msg.contains("DEBUG START"));
        assertThat(logMessages).anyMatch(msg -> msg.contains("DEBUG ERROR"));
        assertThat(logMessages)
                .anyMatch(message ->
                message.contains("DealServiceImpl.getDealById"));
        assertThat(logMessages.stream().filter(msg -> msg.contains("DealServiceImpl.getDealById")).toList()).hasSize(2);
        assertThat(logMessages)
                .anyMatch(message -> message.contains("Deal not found with id: " + id));

    }

}
