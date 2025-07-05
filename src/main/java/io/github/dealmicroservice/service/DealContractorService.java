package io.github.dealmicroservice.service;


import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.model.dto.ContractorDTO;
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

@Service
@Slf4j
public class DealContractorService {

    private final DealContractorRepository dealContractorRepository;
    private final ContractorToRoleRepository contractorToRoleRepository;
    private final ContractorRoleRepository contractorRoleRepository;
    private final DealRepository dealRepository;

    public DealContractorService(DealContractorRepository dealContractorRepository, ContractorToRoleRepository contractorToRoleRepository, ContractorRoleRepository contractorRoleRepository, DealRepository dealRepository) {
        this.dealContractorRepository = dealContractorRepository;
        this.contractorToRoleRepository = contractorToRoleRepository;
        this.contractorRoleRepository = contractorRoleRepository;
        this.dealRepository = dealRepository;
    }

    @Transactional
    public void saveDealContractor(UUID dealId, ContractorDTO request) {
        log.info("Saving deal contractor: {}", request);

        dealRepository.findActiveById(dealId)
                .orElseThrow(() -> new EntityNotFoundException("Deal not found with id: " + dealId));

        DealContractor contractor;
        if (request.getId() != null) {
            contractor = dealContractorRepository.findByIdActive(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + request.getId()));
        } else {
            contractor = new DealContractor();
        }

        if (Boolean.TRUE.equals(request.getMain())) {
            Optional<DealContractor> existingMainOpt = dealContractorRepository.findMainContractorByDealId(dealId);

            if (existingMainOpt.isPresent()) {
                DealContractor existingMain = existingMainOpt.get();

                if (!existingMain.getId().equals(contractor.getId())) {
                    existingMain.setMain(false);
                    dealContractorRepository.save(existingMain);
                }
            }
        }

        contractor.setDealId(dealId);
        contractor.setContractorId(request.getContractorId());
        contractor.setName(request.getName());
        contractor.setInn(request.getInn());
        contractor.setMain(Boolean.TRUE.equals(request.getMain()));
        contractor.setModifyDate(LocalDateTime.now());

        dealContractorRepository.save(contractor);
    }

    @Transactional
    public void deleteDealContractor(UUID contractorId) {
        log.info("Deleting deal contractor: {}", contractorId);

        DealContractor contractor = dealContractorRepository.findByIdActive(contractorId)
                .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + contractorId));

        contractor.setIsActive(false);
        contractor.setModifyDate(LocalDateTime.now());

        dealContractorRepository.save(contractor);
    }

    @Transactional
    public void addRoleToContractor(UUID contractorId, String roleId) {
        log.info("Adding role to contractor id: {}", contractorId);

        // Проверяем, что контрагент существует
        dealContractorRepository.findByIdActive(contractorId)
                .orElseThrow(() -> new EntityNotFoundException("Deal contractor not found with id: " + contractorId));

        // Проверяем, что роль существует
        contractorRoleRepository.findByIdIsActive(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Contractor role not found with id: " + roleId));

        // Проверяем, что роль еще не назначена
        contractorToRoleRepository.findByContractorIdAndRoleIdIsActive(
                contractorId, roleId)
                .ifPresent(existing -> {
                    throw new RuntimeException("Role already assigned to contractor");
                });

        ContractorToRole contractorToRole = new ContractorToRole();
        contractorToRole.setContractorId(contractorId);
        contractorToRole.setRoleId(roleId);
        contractorToRole.setIsActive(true);

        contractorToRoleRepository.save(contractorToRole);
    }

    @Transactional
    public void deleteRoleFromContractor(UUID contractorId, String roleId) {
        log.info("Deleting role from contractor id: {}", contractorId);

        ContractorToRole contractorToRole = contractorToRoleRepository
                .findByContractorIdAndRoleIdIsActive(contractorId, roleId)
                .orElseThrow(() -> new RuntimeException("Contractor role assignment not found"));

        contractorToRole.setIsActive(false);
        contractorToRoleRepository.save(contractorToRole);
    }

}
