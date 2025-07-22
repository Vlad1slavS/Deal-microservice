package io.github.dealmicroservice.mapping;

import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.model.dto.DealSumDTO;
import io.github.dealmicroservice.model.dto.RoleDTO;
import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealContractor;
import io.github.dealmicroservice.model.entity.DealStatus;
import io.github.dealmicroservice.model.entity.DealType;
import io.github.dealmicroservice.model.entity.ContractorToRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Класс - mapper для преобразования сущностей Deal (и связных сущностей) в DTO
 */
@Service
@RequiredArgsConstructor
public class DealMapping {

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
            return null;
        }

        return deal.getSums().stream()
                .filter(sum -> Boolean.TRUE.equals(sum.getIsMain()))
                .findFirst()
                .map(sum -> DealSumDTO.builder()
                        .value(sum.getSum())
                        .currency(sum.getCurrencyId())
                        .build())
                .orElse(null);
    }

    private List<DealContractorDTO> mapContractorsToDTO(List<DealContractor> contractors) {
        if (contractors == null) {
            return null;
        }

        return contractors.stream()
                .filter(contractor -> Boolean.TRUE.equals(contractor.getIsActive()))
                .map(this::mapContractorToDTO)
                .collect(Collectors.toList());
    }

    private DealContractorDTO mapContractorToDTO(DealContractor contractor) {
        return DealContractorDTO.builder()
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

