package io.github.dealmicroservice.controller;

import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.service.DealContractorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/deal-contractor")
@Slf4j
public class DealContractorController {

    private final DealContractorService dealContractorService;

    public DealContractorController(DealContractorService dealContractorService) {
        this.dealContractorService = dealContractorService;
    }

    @PutMapping("/save")
    public ResponseEntity<DealContractorDTO> saveContractor(@Valid @RequestBody DealContractorDTO request) {
        log.info("Received request to save deal contractor: {}", request);
        DealContractorDTO savedContractor = dealContractorService.saveDealContractor(request);
        return ResponseEntity.ok(savedContractor);
    }

    @DeleteMapping("/deal-contractor/delete")
    public ResponseEntity<Void> deleteContractor(@RequestParam UUID contractorId) {
        log.info("Received request to delete deal contractor with id: {}", contractorId);
        dealContractorService.deleteDealContractor(contractorId);
        return ResponseEntity.ok().build();
    }

}
