package io.github.dealmicroservice.controller;

import io.github.dealmicroservice.model.dto.ContractorToRoleDTO;
import io.github.dealmicroservice.service.DealContractorService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/contractor-to-role")
@Slf4j
public class ContractorToRoleController {

    private final DealContractorService dealContractorService;

    public ContractorToRoleController(DealContractorService dealContractorService) {
        this.dealContractorService = dealContractorService;
    }

    @PostMapping("/add")
    public ResponseEntity<ContractorToRoleDTO> addContractorRole(@Valid @RequestBody ContractorToRoleDTO request) {
        log.info("Received request to add contractor role: {}", request);
        ContractorToRoleDTO savedContractor = dealContractorService.addRoleToContractor(request.getContractorId(), request.getRoleId());
        return ResponseEntity.ok(savedContractor);

    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteContractorRole(@Valid @RequestBody ContractorToRoleDTO request) {
        log.info("Received request to delete contractor role: {}", request);
        dealContractorService.deleteRoleFromContractor(request.getContractorId(), request.getRoleId());
        return ResponseEntity.ok().build();
    }

}
