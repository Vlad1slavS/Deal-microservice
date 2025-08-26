package io.github.dealmicroservice.service.impl;

import io.github.dealmicroservice.mapping.DealStatusMapping;
import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.model.entity.DealStatus;
import io.github.dealmicroservice.repository.DealStatusRepository;
import io.github.dealmicroservice.service.DealStatusService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealStatusServiceImpl implements DealStatusService {

    private final DealStatusRepository dealStatusRepository;
    private final DealStatusMapping dealStatusMapping;

    private final Logger log = LogManager.getLogger(DealStatusServiceImpl.class);

    public DealStatusServiceImpl(DealStatusRepository dealStatusRepository,
                                 DealStatusMapping dealStatusMapping) {
        this.dealStatusRepository = dealStatusRepository;
        this.dealStatusMapping = dealStatusMapping;
    }

    /**
     * Получает все активные статусы сделок.
     * Кэширует результат на 1 час.
     */
    @Override
    @Cacheable(cacheNames = "deal_metadata", key = "'deal_statuses'", cacheManager = "dealMetadataCacheManager")
    public List<DealStatusDTO> getAllDealStatuses() {

        log.info("Getting all deal statuses from database");

        List<DealStatus> dealStatuses = dealStatusRepository.findAllByIsActiveTrue();

        return dealStatuses.stream()
                .map(dealStatusMapping::mapToDTO)
                .collect(Collectors.toList());
    }

}
