package io.github.dealmicroservice.service;

//import io.github.auditlib.annotation.AuditLog;
import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.mapping.DealMapping;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.repository.DealRepository;
import io.github.dealmicroservice.repository.DealStatusRepository;
import io.github.dealmicroservice.repository.DealTypeRepository;
import io.github.dealmicroservice.repository.DealSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Сервис для управления сделками
 */
@Service
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class DealServiceImpl implements DealService {

    private final Logger log = LogManager.getLogger(DealServiceImpl.class);

    private final DealRepository dealRepository;
    private final DealStatusRepository dealStatusRepository;
    private final DealMapping mappingService;
    private final DealTypeRepository dealTypeRepository;

    public DealServiceImpl(DealRepository dealRepository,
                           DealStatusRepository dealStatusRepository,
                           DealMapping mappingService,
                           DealTypeRepository dealTypeRepository) {
        this.dealRepository = dealRepository;
        this.dealStatusRepository = dealStatusRepository;
        this.mappingService = mappingService;
        this.dealTypeRepository = dealTypeRepository;
    }

    /**
     * Сохраняет новую или обновляет существующую сделку.
     * Для новой сделки устанавливается статус "DRAFT".
     * @AuditLog - аннотация для логирования методов и http запросов
     *
     * @param request DTO с данными для сохранения сделки
     * @return DTO сохраненной сделки
     * @throws EntityNotFoundException если сделка, статус или тип сделки не найдены
     */
    @Transactional
//    @AuditLog(logLevel = AuditLog.LogLevel.DEBUG)
    public DealDTO saveDeal(DealSaveDTO request) {

        log.info("Save deal {}", request);

        Deal deal;

        if (request.getId() != null) {
            deal = dealRepository.findByIdAndIsActiveTrue(request.getId()).orElseThrow(() -> new EntityNotFoundException("Deal not found by id: " + request.getId()));
        } else {
            deal = new Deal();

            dealStatusRepository.findByIdAndIsActiveTrue("DRAFT")
                    .orElseThrow(() -> new EntityNotFoundException("Статус DRAFT не найден"));
            deal.setStatusId("DRAFT");

        }

        deal.setDescription(request.getDescription());
        deal.setAgreementNumber(request.getAgreementNumber());
        deal.setAgreementDate(request.getAgreementDate());
        deal.setAgreementStartDate(request.getAgreementStartDate());
        deal.setAvailabilityDate(request.getAvailabilityDate());
        deal.setCloseDt(request.getCloseDt());
        deal.setModifyDate(LocalDateTime.now());

        dealTypeRepository.findByIdAndIsActiveTrue(request.getTypeId()).orElseThrow(() ->
                new EntityNotFoundException("Тип сделки " + request.getTypeId() + " не найден"));
        deal.setTypeId(request.getTypeId());

        deal = dealRepository.save(deal);

        log.info("Deal {} saved", deal.getId());

        return getDealById(deal.getId());

    }

    /**
     * Изменяет статус сделки.
     *
     * @param id       идентификатор сделки
     * @param statusId идентификатор нового статуса
     * @return DTO обновленной сделки
     * @throws EntityNotFoundException если сделка или статус не найдены
     */
    @Transactional
    public DealDTO changeStatus(UUID id, String statusId) {

        log.info("Changing deal status for id: {}", id);

        Deal deal = dealRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + id));

        dealStatusRepository.findByIdAndIsActiveTrue(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Deal status not found: " + statusId));

        deal.setStatusId(statusId);
        deal.setModifyDate(LocalDateTime.now());

        dealRepository.save(deal);

        return getDealById(deal.getId());

    }

    /**
     * Получает сделку по идентификатору со всеми связанными данными.
     * @AuditLog - аннотация для логирования методов и http запросов
     *
     * @param id идентификатор сделки
     * @return DTO сделки с полной информацией
     * @throws EntityNotFoundException если сделка не найдена
     */
    @Transactional
//    @AuditLog(logLevel = AuditLog.LogLevel.DEBUG)
    public DealDTO getDealById(UUID id) {
        log.info("Getting deal by id: {}", id);

        Deal deal = dealRepository.findActiveByDealIdWithBasicDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + id));

        dealRepository.findByDealIdWithContractors(id);

        if (deal != null) {
            dealRepository.findByDealIdWithSums(id);
        }

        return mappingService.mapToDTO(deal);
    }

    /**
     * Осуществляет поиск сделок по заданным критериям с пагинацией и сортировкой.
     *
     * @param request DTO с параметрами поиска, пагинации и сортировки
     * @return страница с результатами поиска
     */
    @Transactional
    public Page<DealDTO> searchDeals(DealSearchDTO request) {

        log.info("Searching deals with criteria: {}", request);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );

        Specification<Deal> specification = DealSpecification.buildSpecification(request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Deal> dealPage = dealRepository.findAll(
                specification,
                pageable
        );

        Set<UUID> dealIds = dealPage.getContent().stream()
                .map(Deal::getId)
                .collect(Collectors.toSet());

        loadFullDealInformation(dealPage.getContent(), dealIds);

        List<DealDTO> dealDTOs = dealPage.getContent().stream()
                .map(mappingService::mapToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dealDTOs, pageable, dealPage.getTotalElements());

    }

    /**
     * Загружает полную информацию о сделках, суммы и контрагентов.
     */
    private void loadFullDealInformation(List<Deal> deals, Set<UUID> dealIds) {

        Map<UUID, Deal> dealMap = deals.stream()
                .collect(Collectors.toMap(Deal::getId, Function.identity()));

        List<Deal> dealsWithSums = dealRepository.findDealsWithSums(dealIds);
        dealsWithSums.forEach(dealWithSums -> {
            Deal originalDeal = dealMap.get(dealWithSums.getId());
            if (originalDeal != null) {
                originalDeal.setSums(dealWithSums.getSums());
            }
        });

        List<Deal> dealsWithContractors = dealRepository.findDealsWithContractors(dealIds);
        dealsWithContractors.forEach(dealWithContractors -> {
            Deal originalDeal = dealMap.get(dealWithContractors.getId());
            if (originalDeal != null) {
                originalDeal.setContractors(dealWithContractors.getContractors());
            }
        });

    }

}
