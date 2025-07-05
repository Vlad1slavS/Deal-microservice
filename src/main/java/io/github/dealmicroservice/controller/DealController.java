package io.github.dealmicroservice.controller;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.service.DealService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/deal")
@Slf4j
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PutMapping("/save")
    public ResponseEntity<DealDTO> saveDeal(@Valid @RequestBody DealDTO request) {
        log.info("Received request to save deal: {}", request);
        DealDTO savedDeal = dealService.saveDeal(request);
        return ResponseEntity.ok(savedDeal);

    }

}
