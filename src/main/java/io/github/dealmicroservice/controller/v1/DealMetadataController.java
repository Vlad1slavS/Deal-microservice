package io.github.dealmicroservice.controller.v1;

import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.service.DealStatusService;
import io.github.dealmicroservice.service.DealTypeService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Контроллер для работы со справочными данными сделок
 */
@RestController
@RequestMapping("api/v1")
public class DealMetadataController {

    private final Logger log = LogManager.getLogger(DealMetadataController.class);

    private final DealTypeService dealTypeService;
    private final DealStatusService dealStatusService;

    public DealMetadataController(DealTypeService dealTypeService, DealStatusService dealStatusService) {
        this.dealTypeService = dealTypeService;
        this.dealStatusService = dealStatusService;
    }

    @GetMapping("/deal-type/all")
    public ResponseEntity<List<DealTypeDTO>> getAllDealTypes() {
        log.info("Received request to get all deal types");
        List<DealTypeDTO> dealTypes = dealTypeService.getAllDealTypes();
        return ResponseEntity.ok(dealTypes);
    }

    @GetMapping("/deal-status/all")
    public ResponseEntity<List<DealStatusDTO>> getAllDealStatuses() {
        log.info("Received request to get all deal statuses");
        List<DealStatusDTO> dealStatuses = dealStatusService.getAllDealStatuses();
        return ResponseEntity.ok(dealStatuses);
    }

    @PutMapping("/deal-type/save")
    public ResponseEntity<DealTypeDTO> saveDealType(@Valid @RequestBody DealTypeDTO dealType) {
        log.info("Received request to save deal type: {}", dealType);
        DealTypeDTO savedDealType = dealTypeService.saveDealType(dealType);
        return ResponseEntity.ok(savedDealType);
    }

}
