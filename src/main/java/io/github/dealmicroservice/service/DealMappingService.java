package io.github.dealmicroservice.service;

import io.github.dealmicroservice.model.dto.*;
import io.github.dealmicroservice.model.entity.*;
import io.github.dealmicroservice.repository.DealSumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealMappingService {

    private final DealSumRepository dealSumRepository;

    public DealDTO mapToDTO(Deal deal) {
        if (deal == null) {
            return null;
        }

        return DealDTO.builder()
                .id(deal.getId())
                .description(deal.getDescription())
                .agreementNumber(deal.getAgreementNumber())
                .agreementDate(deal.getAgreementDate())
                .agreementStartDate(deal.getAgreementStartDate())
                .availabilityDate(deal.getAvailabilityDate())
                .type(mapTypeToDTO(deal.getType()))
                .status(mapStatusToDTO(deal.getStatus()))
                .sum(mapMainSumToDTO(deal))
                .closeDt(deal.getCloseDt())
                .contractors(mapContractorsToDTO(deal.getContractors()))
                .build();
    }


    private DealTypeDTO mapTypeToDTO(DealType type) {
        if (type == null) {
            return null;
        }

        return DealTypeDTO.builder()
                .id(type.getId())
                .name(type.getName())
                .build();
    }

    private DealStatusDTO mapStatusToDTO(DealStatus status) {
        if (status == null) {
            return null;
        }

        return DealStatusDTO.builder()
                .id(status.getId())
                .name(status.getName())
                .build();
    }

    private DealSumDTO mapMainSumToDTO(Deal deal) {
        if (deal.getSums() == null) {
            return dealSumRepository.findMainSumByDealId(deal.getId())
                    .map(sum -> DealSumDTO.builder()
                            .value(sum.getSum())
                            .currency(sum.getCurrencyId())
                            .build())
                    .orElse(null);
        }

        return deal.getSums().stream()
                .filter(sum -> Boolean.TRUE.equals(sum.getIsMain()) && Boolean.TRUE.equals(sum.getIsActive()))
                .findFirst()
                .map(sum -> DealSumDTO.builder()
                        .value(sum.getSum())
                        .currency(sum.getCurrencyId())
                        .build())
                .orElse(null);
    }

    private List<ContractorDTO> mapContractorsToDTO(List<DealContractor> contractors) {
        if (contractors == null) {
            return null;
        }

        return contractors.stream()
                .filter(contractor -> Boolean.TRUE.equals(contractor.getIsActive()))
                .map(this::mapContractorToDTO)
                .collect(Collectors.toList());
    }

    private ContractorDTO mapContractorToDTO(DealContractor contractor) {
        return ContractorDTO.builder()
                .id(contractor.getId())
                .contractorId(contractor.getContractorId())
                .name(contractor.getName())
                .main(contractor.getMain())
                .roles(mapRolesToDTO(contractor.getRoles()))
                .build();
    }

    private List<RoleDTO> mapRolesToDTO(List<ContractorToRole> roles) {
        if (roles == null) {
            return null;
        }

        return roles.stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()))
                .map(role -> RoleDTO.builder()
                        .id(role.getRole().getId())
                        .name(role.getRole().getName())
                        .category(role.getRole().getCategory())
                        .build())
                .collect(Collectors.toList());
    }
}
