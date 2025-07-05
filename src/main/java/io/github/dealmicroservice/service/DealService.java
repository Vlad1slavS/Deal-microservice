package io.github.dealmicroservice.service;

import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealSumDTO;
import io.github.dealmicroservice.model.entity.*;
import io.github.dealmicroservice.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DealService {

    private final DealRepository dealRepository;
    private final DealStatusRepository dealStatusRepository;
    private final DealMappingService mappingService;
    private final DealSumRepository dealSumRepository;
    private final DealContractorRepository dealContractorRepository;
    private final DealTypeRepository dealTypeRepository;

    public DealService(DealRepository dealRepository, DealStatusRepository dealStatusRepository, DealMappingService mappingService, DealSumRepository dealSumRepository, DealContractorRepository dealContractorRepository, DealTypeRepository dealTypeRepository) {
        this.dealRepository = dealRepository;

        this.dealStatusRepository = dealStatusRepository;
        this.mappingService = mappingService;
        this.dealSumRepository = dealSumRepository;
        this.dealContractorRepository = dealContractorRepository;
        this.dealTypeRepository = dealTypeRepository;
    }

    public DealDTO saveDeal(DealDTO saveDTO) {

        log.info("Сохранение сделки {}", saveDTO);

        Deal deal;

        if (saveDTO.getId() != null ) {
            deal = dealRepository.findActiveById(saveDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Deal not found by id: " + saveDTO.getId()));
        } else {
            deal = new Deal();
        }

        DealStatus status = dealStatusRepository.findByIdAndIsActive("DRAFT")
                .orElseThrow(() -> new EntityNotFoundException("Статус DRAFT не найден"));
        deal.setStatus(status);
        deal.setStatusId(status.getId());

        deal.setDescription(saveDTO.getDescription());
        deal.setAgreementNumber(saveDTO.getAgreementNumber());
        deal.setAgreementDate(saveDTO.getAgreementDate());
        deal.setAgreementStartDate(saveDTO.getAgreementStartDate());
        deal.setAvailabilityDate(saveDTO.getAvailabilityDate());
        deal.setCloseDt(saveDTO.getCloseDt());
        deal.setModifyDate(LocalDateTime.now());

        DealType type = dealTypeRepository.findByIdIsActive(saveDTO.getType().getId())
                .orElseThrow(() -> new EntityNotFoundException("Тип сделки не найден: " + saveDTO.getType().getId()));
        deal.setType(type);

        List<DealSum> sums = dealSumRepository.findSumsByDealId(saveDTO.getId());
        deal.setSums(sums);

        List<DealContractor> contractors = dealContractorRepository.findAllActiveByDealId(saveDTO.getId());
        deal.setContractors(contractors);

        Deal savedDeal = dealRepository.save(deal);

        log.info("Сделка {} сохранена", savedDeal.getId());

        deal = dealRepository.save(deal);

        return getDealById(deal.getId());

    }

//    @Transactional
//    public DealDTO changeStatus(UUID id, String statusId) {
//
//        log.info("Changing deal status for id: {}", id);
//
//        Deal deal = dealRepository.findActiveById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + id));
//
//        dealStatusRepository.findByIdAndIsActive(statusId)
//                .orElseThrow(() -> new EntityNotFoundException("Deal status not found: " + statusId));
//
//        deal.setStatusId(statusId);
//        deal.setModifyDate(LocalDateTime.now());
//
//        dealRepository.save(deal);
//
//        return getDealById(deal.getId());
//
//    }

    @Transactional(readOnly = true)
    public Page<Deal> searchDeals(DealSearchDTO request) {

        log.info("Searching deals with criteria: {}", request);

        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Deal> dealPage = dealRepository.findAll(
                DealSpecification.buildSpecification(request),
                pageable
        );

        return dealPage;
    }


    public DealDTO getDealById(UUID id) {
        log.info("Getting deal by id: {}", id);

        Deal deal = dealRepository.findByIdWithBasicDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + id));

        List<DealSum> sums = dealSumRepository.findSumsByDealId(id);

        List<DealContractor> contractors = dealContractorRepository.findAllActiveByDealId(id);

        deal.setSums(sums);
        deal.setContractors(contractors);

        log.info("Deal loaded with {} sums and {} contractors", sums.size(), contractors.size());

        return mappingService.mapToDTO(deal);
    }

}
