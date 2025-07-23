package io.github.dealmicroservice.controller.v1;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealStatusChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("/default")
public interface DealControllerContract {

    @PutMapping("/save")
    ResponseEntity<DealDTO> saveDeal(DealSaveDTO request);

    @PatchMapping("/change-status")
    ResponseEntity<DealDTO> changeStatus(DealStatusChangeRequest request);

    @GetMapping("/{id}")
    ResponseEntity<DealDTO> getDealById(UUID id);

    @PostMapping("/search")
    ResponseEntity<Page<DealDTO>> searchDeals(DealSearchDTO searchRequest);

}
