package io.github.dealmicroservice.itegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealContractor;
import io.github.dealmicroservice.repository.DealContractorRepository;
import io.github.dealmicroservice.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DealContractorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealContractorRepository dealContractorRepository;

    @Autowired
    private DealRepository dealRepository;

    private UUID testDealId;
    private UUID testContractorId;

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

    @BeforeEach
    void setUp() {

        Deal deal = Deal.builder()
                .description("Deal for sale car")
                .agreementNumber("AG-123")
                .typeId("OTHER")
                .statusId("DRAFT")
                .isActive(true)
                .build();

        Deal savedDeal = dealRepository.save(deal);

        testDealId = savedDeal.getId();

        DealContractor contractor = DealContractor.builder()
                .contractorId("CONTR004")
                .dealId(testDealId)
                .name("Test Contractor")
                .isActive(true)
                .main(true)
                .build();

        DealContractor savedContractor = dealContractorRepository.save(contractor);

        testContractorId = savedContractor.getId();

    }

    @Test
    void findByIdWithDetails_ExistingContractor() {

        Optional<DealContractor> contractor = dealContractorRepository.findByIdActive(testContractorId);

        assertThat(contractor).isPresent();

        DealContractor result = contractor.get();

        assertThat(result.getId()).isEqualTo(testContractorId);
        assertThat(result.getContractorId()).isEqualTo("CONTR004");
        assertThat(result.getName()).isEqualTo("Test Contractor");

    }

    @Test
    void findByIdWithDetails_NonExistentDeal() {

        UUID not_existing_deal_id = UUID.randomUUID();

        Optional<DealContractor> result = dealContractorRepository.findByIdActive(not_existing_deal_id);

        assertThat(result).isEmpty();

    }

    @Test
    void tryAddContractorWhenMainFKAlreadyExists_DataIntegrityViolationException() {
        DealContractor contractor = DealContractor.builder()
                .contractorId("CONTR005")
                .dealId(testDealId)
                .name("Test Contractor")
                .isActive(true)
                .main(true)
                .build();

        assertThatThrownBy(() -> dealContractorRepository.save(contractor))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

}
