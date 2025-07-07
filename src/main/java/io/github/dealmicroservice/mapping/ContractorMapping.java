package io.github.dealmicroservice.mapping;

import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.model.dto.RoleDTO;
import io.github.dealmicroservice.model.entity.ContractorToRole;
import io.github.dealmicroservice.model.entity.DealContractor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс - mapper для преобразования сущностей DealContractor (и связных сущностей) в DTO
 */
@Service
public class ContractorMapping {

    public DealContractorDTO mapToDTO(DealContractor deal) {
        if (deal == null) {
            return null;
        }

        return DealContractorDTO.builder()
                .id(deal.getId())
                .dealId(deal.getDealId())
                .contractorId(deal.getContractorId())
                .name(deal.getName())
                .inn(deal.getInn())
                .main(deal.getMain())
                .roles(mapRolesToDTO(deal.getRoles()))
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
