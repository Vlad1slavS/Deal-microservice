package io.github.dealmicroservice.service;

import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.mapping.ContractorMapping;
import io.github.dealmicroservice.model.dto.ContractorToRoleDTO;
import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.model.entity.ContractorToRole;
import io.github.dealmicroservice.model.entity.DealContractor;
import io.github.dealmicroservice.repository.ContractorRoleRepository;
import io.github.dealmicroservice.repository.ContractorToRoleRepository;
import io.github.dealmicroservice.repository.DealContractorRepository;
import io.github.dealmicroservice.repository.DealRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления контрагентами в сделках.
 */
@Service
@Slf4j
public class DealContractorServiceImpl implements DealContractorService {

    private final DealContractorRepository dealContractorRepository;
    private final ContractorToRoleRepository contractorToRoleRepository;
    private final ContractorRoleRepository contractorRoleRepository;
    private final DealRepository dealRepository;
    private final ContractorMapping mappingService;

    public DealContractorServiceImpl(DealContractorRepository dealContractorRepository,
                                     ContractorToRoleRepository contractorToRoleRepository,
                                     ContractorRoleRepository contractorRoleRepository,
                                     DealRepository dealRepository,
                                     ContractorMapping mappingService) {
        this.dealContractorRepository = dealContractorRepository;
        this.contractorToRoleRepository = contractorToRoleRepository;
        this.contractorRoleRepository = contractorRoleRepository;
        this.dealRepository = dealRepository;
        this.mappingService = mappingService;
    }

    /**
     * Сохраняет или обновляет контрагента сделки.
     * Если указан текущий контрагент как основной, предыдущий основной контрагент становится неосновным.
     *
     * @param request DTO с данными контрагента
     * @return DTO сохраненного контрагента
     * @throws EntityNotFoundException если сделка или контрагент не найдены
     */
    @Transactional
    public DealContractorDTO saveDealContractor(DealContractorDTO request) {
        log.info("Saving deal contractor: {}", request);

        dealRepository.findByIdAndIsActiveTrue(request.getDealId())
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + request.getDealId()));

        DealContractor contractor;
        if (request.getId() != null) {
            contractor = dealContractorRepository.findByIdAndIsActiveTrue(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + request.getId()));
        } else {
            contractor = new DealContractor();
        }

        if (Boolean.TRUE.equals(request.getMain())) {
            Optional<DealContractor> existingMainOpt = dealContractorRepository.findByDealIdAndMainTrueAndIsActiveTrue(request.getDealId());

            if (existingMainOpt.isPresent()) {
                DealContractor existingMain = existingMainOpt.get();

                if (!existingMain.getId().equals(contractor.getId())) {
                    existingMain.setMain(false);
                    dealContractorRepository.save(existingMain);
                }
            }
        }

        contractor.setDealId(request.getDealId());
        contractor.setContractorId(request.getContractorId());
        contractor.setName(request.getName());
        contractor.setInn(request.getInn());
        contractor.setMain(Boolean.TRUE.equals(request.getMain()));
        contractor.setModifyDate(LocalDateTime.now());

        DealContractor savedContractor = dealContractorRepository.save(contractor);

        return mappingService.mapToDTO(savedContractor);
    }

    /**
     * Помечает контрагента сделки как неактивного
     *
     * @param contractorId идентификатор контрагента
     * @throws EntityNotFoundException если контрагент не найден
     */
    @Transactional
    public void deleteDealContractor(UUID contractorId) {
        log.info("Deleting deal contractor: {}", contractorId);

        DealContractor contractor = dealContractorRepository.findByIdAndIsActiveTrue(contractorId)
                .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + contractorId));

        contractor.setIsActive(false);
        contractor.setModifyDate(LocalDateTime.now());

        dealContractorRepository.save(contractor);
    }

    /**
     * Добавляет роль контрагенту в сделке
     *
     * @param contractorId идентификатор контрагента
     * @param roleId идентификатор роли
     * @return DTO с информацией о назначенной роли
     * @throws EntityNotFoundException если контрагент или роль не найдены
     */
    @Transactional
    public ContractorToRoleDTO addRoleToContractor(UUID contractorId, String roleId) {
        log.info("Adding role to contractor id: {}", contractorId);

        dealContractorRepository.findByIdAndIsActiveTrue(contractorId)
                .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + contractorId));

        contractorRoleRepository.findByIdAndIsActiveTrue(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Contractor role not found with id: " + roleId));

        ContractorToRole contractorToRole = new ContractorToRole();
        contractorToRole.setContractorId(contractorId);
        contractorToRole.setRoleId(roleId);
        contractorToRole.setIsActive(true);

        contractorToRoleRepository.save(contractorToRole);

        return ContractorToRoleDTO.builder()
                .contractorId(contractorId)
                .roleId(roleId)
                .build();
    }

    /**
     * Удаляет роль у контрагента в сделке
     *
     * @param contractorId идентификатор контрагента
     * @param roleId идентификатор роли
     * @throws EntityNotFoundException если связь контрагента с ролью не найдена
     */
    @Transactional
    public void deleteRoleFromContractor(UUID contractorId, String roleId) {
        log.info("Deleting role from contractor id: {}", contractorId);

        ContractorToRole contractorToRole = contractorToRoleRepository
                .findByContractorIdAndRoleIdAndIsActiveTrue(contractorId, roleId)
                .orElseThrow(() -> new EntityNotFoundException("Contractor role assignment not found"));

        contractorToRole.setIsActive(false);
        contractorToRoleRepository.save(contractorToRole);
    }

}
