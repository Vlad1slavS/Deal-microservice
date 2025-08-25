package io.github.dealmicroservice.itegration.cache;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealStatus;
import io.github.dealmicroservice.model.entity.DealType;
import io.github.dealmicroservice.repository.DealRepository;
import io.github.dealmicroservice.repository.DealStatusRepository;
import io.github.dealmicroservice.repository.DealTypeRepository;
import io.github.dealmicroservice.service.DealService;
import io.github.dealmicroservice.mapping.DealMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Интеграционный тест для проверки кэширования DealService
 */
@SpringBootTest
@Testcontainers
class DealServiceCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("deal_service_test_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private DealService dealService;

    @MockitoBean
    private DealRepository dealRepository;

    @MockitoBean
    private DealStatusRepository dealStatusRepository;

    @MockitoBean
    private DealTypeRepository dealTypeRepository;

    @MockitoBean
    private DealMapping dealMapping;

    @Autowired
    private CacheManager cacheManager;

    private UUID testDealId;
    private Deal testDeal;
    private DealDTO testDealDTO;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("deals").clear();
        Mockito.reset(dealRepository, dealStatusRepository, dealTypeRepository, dealMapping);

        testDealId = UUID.randomUUID();
        testDeal = Deal.builder()
                .id(testDealId)
                .description("Test Deal")
                .typeId("ACTIVE")
                .statusId("DRAFT")
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .build();

        testDealDTO = DealDTO.builder()
                .id(testDealId)
                .description("Test Deal")
                .build();

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var cache = cacheManager.getCache("deals");
                    assertThat(cache).isNotNull();
                    assertThat(cache.get(testDealId.toString())).isNull();
                });
    }

    @Test
    void doubleMethodCall_CallRepositoryOnceAndCacheResult() {
        when(dealRepository.findActiveByDealIdWithBasicDetails(testDealId)).thenReturn(Optional.of(testDeal));
        when(dealRepository.findByDealIdWithContractors(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealRepository.findByDealIdWithSums(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealMapping.mapToDTO(testDeal)).thenReturn(testDealDTO);

        DealDTO result1 = dealService.getDealById(testDealId);
        DealDTO result2 = dealService.getDealById(testDealId);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getId()).isEqualTo(testDealId);
        assertThat(result2.getId()).isEqualTo(testDealId);

        verify(dealRepository, times(1)).findActiveByDealIdWithBasicDetails(testDealId);
        verify(dealMapping, times(1)).mapToDTO(testDeal);

        assertThat(cacheManager.getCache("deals").get(testDealId.toString())).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnSaveDeal() {
        DealSaveDTO saveDTO = DealSaveDTO.builder()
                .id(testDealId)
                .description("Updated Deal")
                .typeId("ACTIVE")
                .build();

        DealType dealType = new DealType("ACTIVE", "Active Deal", true);

        when(dealRepository.findByIdAndIsActiveTrue(testDealId)).thenReturn(Optional.of(testDeal));
        when(dealTypeRepository.findByIdAndIsActiveTrue("ACTIVE")).thenReturn(Optional.of(dealType));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
        when(dealRepository.findActiveByDealIdWithBasicDetails(testDealId)).thenReturn(Optional.of(testDeal));
        when(dealRepository.findByDealIdWithContractors(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealRepository.findByDealIdWithSums(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealMapping.mapToDTO(testDeal)).thenReturn(testDealDTO);

        dealService.getDealById(testDealId);
        verify(dealRepository, times(1)).findActiveByDealIdWithBasicDetails(testDealId);

        dealService.saveDeal(saveDTO);

        dealService.getDealById(testDealId);

        verify(dealRepository, times(3)).findActiveByDealIdWithBasicDetails(testDealId);
        assertThat(cacheManager.getCache("deals").get(testDealId.toString())).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnChangeStatus() {
        DealStatus newStatus = new DealStatus("ACTIVE", "Active", true);

        when(dealRepository.findByIdAndIsActiveTrue(testDealId)).thenReturn(Optional.of(testDeal));
        when(dealStatusRepository.findByIdAndIsActiveTrue("ACTIVE")).thenReturn(Optional.of(newStatus));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
        when(dealRepository.findActiveByDealIdWithBasicDetails(testDealId)).thenReturn(Optional.of(testDeal));
        when(dealRepository.findByDealIdWithContractors(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealRepository.findByDealIdWithSums(testDealId)).thenReturn(Optional.ofNullable(testDeal));
        when(dealMapping.mapToDTO(testDeal)).thenReturn(testDealDTO);

        dealService.getDealById(testDealId);
        verify(dealRepository, times(1)).findActiveByDealIdWithBasicDetails(testDealId);

        dealService.changeStatus(testDealId, "ACTIVE");

        dealService.getDealById(testDealId);

        verify(dealRepository, times(3)).findActiveByDealIdWithBasicDetails(testDealId);
        assertThat(cacheManager.getCache("deals").get(testDealId.toString())).isNotNull();
    }
}