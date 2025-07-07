package io.github.dealmicroservice.controller;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealStatusChangeRequest;
import io.github.dealmicroservice.service.DealService;
import io.github.dealmicroservice.service.ExcelService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/deal")
@Slf4j
public class DealController {

    private final DealService dealService;
    private final ExcelService excelService;

    public DealController(DealService dealService, ExcelService excelService) {
        this.dealService = dealService;
        this.excelService = excelService;
    }

    @PutMapping("/save")
    public ResponseEntity<DealDTO> saveDeal(@Valid @RequestBody DealSaveDTO request) {
        log.info("Received request to save deal: {}", request);
        DealDTO savedDeal = dealService.saveDeal(request);
        return ResponseEntity.ok(savedDeal);
    }

    @PatchMapping("/change-status")
    public ResponseEntity<DealDTO> changeStatus(@Valid @RequestBody DealStatusChangeRequest request) {
        log.info("Received request to change deal status: {}", request);
        DealDTO changedDeal = dealService.changeStatus(request.getId(), request.getStatusId());
        return ResponseEntity.ok(changedDeal);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealDTO> getDealById(@PathVariable UUID id) {
        log.info("Received request to get deal by id: {}", id);
        DealDTO deal = dealService.getDealById(id);
        return ResponseEntity.ok(deal);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<DealDTO>> searchDeals(@RequestBody DealSearchDTO request) {
        log.info("Received request to search deals: {}", request);
        Page<DealDTO> deals = dealService.searchDeals(request);
        return ResponseEntity.ok(deals);
    }

    @PostMapping("/search/export")
    public ResponseEntity<String> exportDeals(@RequestBody DealSearchDTO request) throws IOException {
        log.info("Received request to export deals: {}", request);
        String filePath = excelService.exportDealsToExcel(request);
        return ResponseEntity.ok(filePath);

    }

}
