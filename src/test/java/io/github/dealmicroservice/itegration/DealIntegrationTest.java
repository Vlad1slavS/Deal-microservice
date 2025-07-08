package io.github.dealmicroservice.itegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.repository.DealRepository;
import io.github.dealmicroservice.repository.DealSpecification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DealIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealRepository dealRepository;

    private UUID testDealId;

    @BeforeEach
    void setUp() {

        Deal deal = Deal.builder()
                .description("Deal for sale car")
                .agreementNumber("AG-123")
                .typeId("OTHER")
                .statusId("DRAFT")
                .isActive(true)
                .build();

        Deal deal2 = Deal.builder()
                .description("Apartment sale deal")
                .agreementNumber("AG-3456")
                .typeId("OVERDRAFT")
                .statusId("DRAFT")
                .isActive(true)
                .build();

        Deal deal3 = Deal.builder()
                .description("Credit deal")
                .agreementNumber("AG-236")
                .typeId("CREDIT")
                .statusId("DRAFT")
                .isActive(true)
                .build();

        Deal savedDeal = dealRepository.save(deal);
        dealRepository.save(deal2);
        dealRepository.save(deal3);

        testDealId = savedDeal.getId();
    }

    @AfterEach
    void tearDown() {
        dealRepository.deleteAll();
    }

    @Test
    void findByIdWithDetails_ExistingDeal() {
        Optional<Deal> deal = dealRepository.findActiveByIdWithBasicDetails(testDealId);

        assertThat(deal).isPresent();

        Deal result = deal.get();

        dealRepository.findByIdWithSums(testDealId);
        dealRepository.findByIdWithContractors(testDealId);

        assertThat(result.getId()).isEqualTo(testDealId);
        assertThat(result.getDescription()).isEqualTo("Deal for sale car");
        assertThat(result.getAgreementNumber()).isEqualTo("AG-123");

    }

    @Test
    void findByIdWithDetails_NonExistentDeal() {

        UUID not_existing_deal_id = UUID.randomUUID();

        Optional<Deal> result = dealRepository.findActiveByIdWithBasicDetails(not_existing_deal_id);

        assertThat(result).isEmpty();

    }

    @Test
    void searchDeals_NoFilters_Success() {

        DealSearchDTO filter = new DealSearchDTO();
        filter.setPage(0);
        filter.setSize(10);

        Specification<Deal> specification = DealSpecification.buildSpecification(filter);

        Page<Deal> result = dealRepository.findAll(specification, PageRequest.of(0, 10));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(result.getSize()).isEqualTo(10);

    }

    @Test
    void searchDeals_DescriptionFilters_Success() {

        DealSearchDTO filter = new DealSearchDTO();
        filter.setDescription("Apartment sale deal");
        filter.setPage(0);
        filter.setSize(10);

        Specification<Deal> specification = DealSpecification.buildSpecification(filter);

        Page<Deal> result = dealRepository.findAll(specification, PageRequest.of(0, 10));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().getFirst().getDescription()).isEqualTo("Apartment sale deal");
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getSize()).isEqualTo(10);

    }

    @Test
    void searchDeals_TypeListFilters_Success() {

        List<String> type = Arrays.asList("OVERDRAFT", "OTHER");

        DealSearchDTO filter = new DealSearchDTO();
        filter.setType(type);
        filter.setPage(0);
        filter.setSize(10);

        Specification<Deal> specification = DealSpecification.buildSpecification(filter);

        Page<Deal> result = dealRepository.findAll(specification, PageRequest.of(0, 10));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(result.getSize()).isEqualTo(10);

    }

}