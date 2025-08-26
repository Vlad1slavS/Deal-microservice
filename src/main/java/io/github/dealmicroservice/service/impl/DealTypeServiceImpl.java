package io.github.dealmicroservice.service.impl;

import io.github.dealmicroservice.mapping.DealTypeMapping;
import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.model.entity.DealType;
import io.github.dealmicroservice.repository.DealTypeRepository;
import io.github.dealmicroservice.service.DealTypeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DealTypeServiceImpl implements DealTypeService {

    private final DealTypeRepository dealTypeRepository;
    private final DealTypeMapping dealTypeMapping;

    private final Logger log = LogManager.getLogger(DealTypeServiceImpl.class);

    public DealTypeServiceImpl(DealTypeRepository dealTypeRepository, DealTypeMapping dealTypeMapping) {
        this.dealTypeRepository = dealTypeRepository;
        this.dealTypeMapping = dealTypeMapping;
    }

    /**
     * Получает все активные типы сделок.
     * Кэширует результат на 1 час.
     */
    @Override
    @Cacheable(cacheNames = "deal_metadata", key = "'deal_types'", cacheManager = "dealMetadataCacheManager")
    public List<DealTypeDTO> getAllDealTypes() {

        log.info("Getting all deal types from database");

        List<DealType> dealTypes = dealTypeRepository.findAllByIsActiveTrue();

        return dealTypes.stream()
                .map(dealTypeMapping::mapToDTO)
                .collect(Collectors.toList());

    }

    /**
     * Сохраняет/обновляет имя типа сделки.
     * Очищает кэш справочников после сохранения.
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "deal_metadata", key = "'deal_types'", cacheManager = "dealMetadataCacheManager")
    public DealTypeDTO saveDealType(DealTypeDTO dealTypeDTO) {

        if (dealTypeDTO.getId() == null) {
            throw new IllegalArgumentException("Deal type ID must not be null");
        }

        log.info("Saving deal type: {}", dealTypeDTO);

        Optional<DealType> existingDealType = dealTypeRepository.findById(dealTypeDTO.getId());

        DealType dealType;
        if (existingDealType.isPresent()) {
            dealType = existingDealType.get();
            dealType.setName(dealTypeDTO.getName());
        } else {
            dealType = new DealType();
            dealType.setId(dealTypeDTO.getId());
            dealType.setName(dealTypeDTO.getName());
            dealType.setIsActive(true);
        }

        DealType saved = dealTypeRepository.save(dealType);

        log.info("Deal type saved: {}", saved.getId());

        return dealTypeMapping.mapToDTO(saved);
    }

}
