package io.github.dealmicroservice.itegration.cache;

import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.model.entity.DealType;
import io.github.dealmicroservice.model.entity.DealStatus;
import io.github.dealmicroservice.repository.DealTypeRepository;
import io.github.dealmicroservice.repository.DealStatusRepository;
import io.github.dealmicroservice.service.DealStatusService;
import io.github.dealmicroservice.service.DealTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Интеграционный тест для проверки кэширования справочной информации (DealType и DealStatus)
 */
@SpringBootTest
@Testcontainers
class DealMetadataCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("deal_metadata_test_db")
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
    private DealTypeService dealTypeService;

    @Autowired
    private DealStatusService dealStatusService;

    @MockitoBean
    private DealTypeRepository dealTypeRepository;

    @MockitoBean
    private DealStatusRepository dealStatusRepository;

    @Autowired
    @Qualifier("dealMetadataCacheManager")
    private CacheManager dealMetadataCacheManager;

    @BeforeEach
    void setUp() {
        dealMetadataCacheManager.getCache("deal_metadata").clear();
        Mockito.reset(dealTypeRepository, dealStatusRepository);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var cache = dealMetadataCacheManager.getCache("deal_metadata");
                    assertThat(cache).isNotNull();
                    assertThat(cache.get("deal_types")).isNull();
                    assertThat(cache.get("deal_statuses")).isNull();
                });
    }

    @Test
    void doubleMethodCall_CallRepositoryOnceForDealTypes() {
        List<DealType> dealTypes = Arrays.asList(
                new DealType("CREDIT", "Кредитная сделка", true),
                new DealType("OVERDRAFT", "Овердрафт", true)
        );
        when(dealTypeRepository.findAllByIsActiveTrue()).thenReturn(dealTypes);

        List<DealTypeDTO> result1 = dealTypeService.getAllDealTypes();
        List<DealTypeDTO> result2 = dealTypeService.getAllDealTypes();

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result1.get(0).getName()).isEqualTo("Кредитная сделка");

        verify(dealTypeRepository, times(1)).findAllByIsActiveTrue();

        assertThat(dealMetadataCacheManager.getCache("deal_metadata").get("deal_types")).isNotNull();
    }

    @Test
    void doubleMethodCall_CallRepositoryOnceForDealStatuses() {
        List<DealStatus> dealStatuses = Arrays.asList(
                new DealStatus("DRAFT", "Черновик", true),
                new DealStatus("ACTIVE", "Активная", true),
                new DealStatus("CLOSED", "Закрытая", true)
        );
        when(dealStatusRepository.findAllByIsActiveTrue()).thenReturn(dealStatuses);

        List<DealStatusDTO> result1 = dealStatusService.getAllDealStatuses();
        List<DealStatusDTO> result2 = dealStatusService.getAllDealStatuses();

        assertThat(result1).hasSize(3);
        assertThat(result2).hasSize(3);
        assertThat(result1.get(0).getName()).isEqualTo("Черновик");

        verify(dealStatusRepository, times(1)).findAllByIsActiveTrue();

        assertThat(dealMetadataCacheManager.getCache("deal_metadata").get("deal_statuses")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictDealTypesCacheOnSave() {
        List<DealType> dealTypes = Arrays.asList(
                new DealType("CREDIT", "Кредитная сделка", true)
        );
        when(dealTypeRepository.findAllByIsActiveTrue()).thenReturn(dealTypes);

        DealType newDealType = new DealType("OVERDRAFT", "Овердрафт", true);
        when(dealTypeRepository.save(any(DealType.class))).thenReturn(newDealType);

        dealTypeService.getAllDealTypes();
        verify(dealTypeRepository, times(1)).findAllByIsActiveTrue();

        DealTypeDTO newDealTypeDTO = DealTypeDTO.builder()
                .id("OVERDRAFT")
                .name("Овердрафт")
                .build();
        dealTypeService.saveDealType(newDealTypeDTO);

        dealTypeService.getAllDealTypes();

        verify(dealTypeRepository, times(2)).findAllByIsActiveTrue();
        assertThat(dealMetadataCacheManager.getCache("deal_metadata").get("deal_types")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictDealTypesCacheOnUpdateExisting() {
        DealType existingDealType = new DealType("CREDIT", "Кредитная сделка", true);
        List<DealType> dealTypes = Arrays.asList(existingDealType);

        when(dealTypeRepository.findAllByIsActiveTrue()).thenReturn(dealTypes);
        when(dealTypeRepository.findByIdAndIsActiveTrue("CREDIT")).thenReturn(Optional.of(existingDealType));
        when(dealTypeRepository.save(any(DealType.class))).thenReturn(existingDealType);

        dealTypeService.getAllDealTypes();
        verify(dealTypeRepository, times(1)).findAllByIsActiveTrue();

        DealTypeDTO updatedDealTypeDTO = DealTypeDTO.builder()
                .id("CREDIT")
                .name("Обновленная кредитная сделка")
                .build();
        dealTypeService.saveDealType(updatedDealTypeDTO);

        dealTypeService.getAllDealTypes();

        verify(dealTypeRepository, times(2)).findAllByIsActiveTrue();
    }

    @Test
    void doubleCacheTest_DealTypesAndStatuses() {
        List<DealType> dealTypes = Arrays.asList(
                new DealType("CREDIT", "Кредитная сделка", true)
        );
        List<DealStatus> dealStatuses = Arrays.asList(
                new DealStatus("DRAFT", "Черновик", true)
        );

        when(dealTypeRepository.findAllByIsActiveTrue()).thenReturn(dealTypes);
        when(dealStatusRepository.findAllByIsActiveTrue()).thenReturn(dealStatuses);

        dealTypeService.getAllDealTypes();
        dealStatusService.getAllDealStatuses();

        verify(dealTypeRepository, times(1)).findAllByIsActiveTrue();
        verify(dealStatusRepository, times(1)).findAllByIsActiveTrue();

        DealType newDealType = new DealType("OVERDRAFT", "Овердрафт", true);
        when(dealTypeRepository.save(any(DealType.class))).thenReturn(newDealType);

        DealTypeDTO newDealTypeDTO = DealTypeDTO.builder()
                .id("OVERDRAFT")
                .name("Овердрафт")
                .build();
        dealTypeService.saveDealType(newDealTypeDTO);

        dealTypeService.getAllDealTypes();
        dealStatusService.getAllDealStatuses();

        verify(dealTypeRepository, times(2)).findAllByIsActiveTrue();
        verify(dealStatusRepository, times(1)).findAllByIsActiveTrue();
    }
}
