package io.github.dealmicroservice.mapping;

import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.model.entity.DealType;
import org.springframework.stereotype.Service;

@Service
public class DealTypeMapping {

    public DealTypeDTO mapToDTO(DealType dt) {
        if (dt == null) {
            return null;
        }
        return DealTypeDTO.builder()
                .id(dt.getId())
                .name(dt.getName())
                .build();
    }

}
