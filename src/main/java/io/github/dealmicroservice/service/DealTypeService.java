package io.github.dealmicroservice.service;

import io.github.dealmicroservice.model.dto.DealTypeDTO;

import java.util.List;

public interface DealTypeService {

    List<DealTypeDTO> getAllDealTypes();

    DealTypeDTO saveDealType(DealTypeDTO dealType);

}
