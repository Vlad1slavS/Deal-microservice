package io.github.dealmicroservice.service;

import io.github.dealmicroservice.model.dto.DealStatusDTO;

import java.util.List;

public interface DealStatusService {

    List<DealStatusDTO> getAllDealStatuses();

}
