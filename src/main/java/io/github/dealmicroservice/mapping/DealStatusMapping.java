package io.github.dealmicroservice.mapping;

import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.model.entity.DealStatus;
import org.springframework.stereotype.Service;

@Service
public class DealStatusMapping {

    public DealStatusDTO mapToDTO(DealStatus ds) {
        if (ds == null) {
            return null;
        }

        return DealStatusDTO.builder()
                .id(ds.getId())
                .name(ds.getName())
                .build();
    }

}
