package io.github.dealmicroservice.controller;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealStatusChangeRequest;
import io.github.dealmicroservice.service.DealService;
import io.github.dealmicroservice.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Deals", description = "API для управления сделками")
public class DealController {

    private final DealService dealService;
    private final ExcelService excelService;

    public DealController(DealService dealService, ExcelService excelService) {
        this.dealService = dealService;
        this.excelService = excelService;
    }

    @Operation(summary = "Создать или обновить сделку")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделка создана или обновлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples =
                                    @ExampleObject(
                                            name = "Изменение сделки (указывается ID существующей сделки)",
                                            value = """
                                            {
                                              "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                              "description": "13324556",
                                              "type": {
                                                "id": "OTHER",
                                                "name": "Иное"
                                               },
                                              "status": {
                                                    "id": "DRAFT",
                                                    "name": "Черновик"
                                                  },
                                              "sum": {
                                                    "value": 10.20,
                                                    "currency": "USD"
                                                  },
                                              "contractors": [
                                                    {
                                                      "id": "73594325-a36a-4e14-9a05-0155b200a3c3",
                                                      "name": "ПАО Банк Финансы",
                                                      "inn": null,
                                                      "main": true,
                                                      "roles": [
                                                        {
                                                          "id": "DRAWER",
                                                          "name": "Векселедатель",
                                                          "category": "BORROWER"
                                                        }
                                                      ],
                                                      "deal_id": null,
                                                      "contractor_id": "CONTR004"
                                                    }
                                                  ],
                                                  "agreement_number": "111",
                                                  "agreement_date": "2025-06-05",
                                                  "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                  "availability_date": "2025-06-05",
                                                  "close_dt": "2025-06-05T21:10:06.362433"
                                                }
                                            """
                                    )
                    )

            )
    })
    @PutMapping("/save")
    public ResponseEntity<DealDTO> saveDeal(
            @Parameter(description = "Сущность для сохранения сделки",
                    schema = @Schema(implementation = DealSaveDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сущность для сохранения сделки",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealSaveDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Пример для создания сделки",
                                            value = """
                                                {
                                                    "description": "13378",
                                                    "agreement_number": "123456789",
                                                    "agreement_date": "2025-06-05T21:10:06.362433",
                                                    "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                    "availability_date": "2025-07-05T21:10:06.362433",
                                                    "typeId": "ACTIVE",
                                                    "close_dt": "2025-07-05T21:10:06.362433"
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Пример для обновления сделки",
                                            value = """
                                                {
                                                    "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                    "description": "13324556",
                                                    "typeId": "OTHER",
                                                    "agreement_number": "111",
                                                    "agreement_date": "2025-06-05T21:10:06.362433",
                                                    "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                    "availability_date": "2025-06-05T21:10:06.362433",
                                                    "close_dt": "2025-06-05T21:10:06.362433"
                                                }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody DealSaveDTO request) {
        log.info("Received request to save deal: {}", request);
        DealDTO savedDeal = dealService.saveDeal(request);
        return ResponseEntity.ok(savedDeal);
    }

    @Operation(summary = "Изменить статус сделки")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус сделки изменен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                              "description": "13324556",
                                              "type": {
                                                "id": "OTHER",
                                                "name": "Иное"
                                               },
                                              "status": {
                                                    "id": "DRAFT",
                                                    "name": "Черновик"
                                                  },
                                              "sum": {
                                                    "value": 10.20,
                                                    "currency": "USD"
                                                  },
                                              "contractors": [
                                                    {
                                                      "id": "73594325-a36a-4e14-9a05-0155b200a3c3",
                                                      "name": "ПАО Банк Финансы",
                                                      "inn": null,
                                                      "main": true,
                                                      "roles": [
                                                        {
                                                          "id": "DRAWER",
                                                          "name": "Векселедатель",
                                                          "category": "BORROWER"
                                                        }
                                                      ],
                                                      "deal_id": null,
                                                      "contractor_id": "CONTR004"
                                                    }
                                                  ],
                                                  "agreement_number": "111",
                                                  "agreement_date": "2025-06-05",
                                                  "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                  "availability_date": "2025-06-05",
                                                  "close_dt": "2025-06-05T21:10:06.362433"
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal not found with id: 999",
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal status not found: 999",
                                    }
                                    """
                            )
                    )
            )
    })
    @PatchMapping("/change-status")
    public ResponseEntity<DealDTO> changeStatus(@Valid @RequestBody DealStatusChangeRequest request) {
        log.info("Received request to change deal status: {}", request);
        DealDTO changedDeal = dealService.changeStatus(request.getId(), request.getStatusId());
        return ResponseEntity.ok(changedDeal);
    }

    @Operation(summary = "Получить сделку по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделка найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                              "description": "13324556",
                                              "type": {
                                                "id": "OTHER",
                                                "name": "Иное"
                                               },
                                              "status": {
                                                    "id": "DRAFT",
                                                    "name": "Черновик"
                                                  },
                                              "sum": {
                                                    "value": 10.20,
                                                    "currency": "USD"
                                                  },
                                              "contractors": [
                                                    {
                                                      "id": "73594325-a36a-4e14-9a05-0155b200a3c3",
                                                      "name": "ПАО Банк Финансы",
                                                      "inn": null,
                                                      "main": true,
                                                      "roles": [
                                                        {
                                                          "id": "DRAWER",
                                                          "name": "Векселедатель",
                                                          "category": "BORROWER"
                                                        }
                                                      ],
                                                      "deal_id": null,
                                                      "contractor_id": "CONTR004"
                                                    }
                                                  ],
                                                  "agreement_number": "111",
                                                  "agreement_date": "2025-06-05",
                                                  "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                  "availability_date": "2025-06-05",
                                                  "close_dt": "2025-06-05T21:10:06.362433"
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal not found with id: 999",
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DealDTO> getDealById(@PathVariable UUID id) {
        log.info("Received request to get deal by id: {}", id);
        DealDTO deal = dealService.getDealById(id);
        return ResponseEntity.ok(deal);
    }

    @Operation(summary = "Поиск сделок с пагинацией и фильтрами")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделки найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Найденная страница со сделками",
                                    value = """
                                        {
                                          "content": [
                                            {
                                                  "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                  "description": "13324556",
                                                  "type": {
                                                    "id": "OTHER",
                                                    "name": "Иное"
                                                  },
                                                  "status": {
                                                    "id": "DRAFT",
                                                    "name": "Черновик"
                                                  },
                                                  "sum": {
                                                    "value": 10.20,
                                                    "currency": "USD"
                                                  },
                                                  "contractors": [
                                                    {
                                                      "id": "73594325-a36a-4e14-9a05-0155b200a3c3",
                                                      "name": "ПАО Банк Финансы",
                                                      "inn": null,
                                                      "main": true,
                                                      "roles": [
                                                        {
                                                          "id": "DRAWER",
                                                          "name": "Векселедатель",
                                                          "category": "BORROWER"
                                                        }
                                                      ],
                                                      "deal_id": null,
                                                      "contractor_id": "CONTR004"
                                                    }
                                                  ],
                                                  "agreement_number": "111",
                                                  "agreement_date": "2025-06-05",
                                                  "agreement_start_date": "2025-06-05T21:10:06.362433",
                                                  "availability_date": "2025-06-05",
                                                  "close_dt": "2025-06-05T21:10:06.362433"
                                                }
                                          ],
                                            "page": {
                                                "size": 10,
                                                "number": 0,
                                                "totalElements": 3,
                                                "totalPages": 1
                                              }
                                        }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/search")
    public ResponseEntity<Page<DealDTO>> searchDeals(
            @Parameter(description = "Фильтр поиска",
                    schema = @Schema(implementation = DealSearchDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Фильтр поиска сделок",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealSearchDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Фильтр поиска",
                                            value = """
                                                {
                                                  "description": "description",
                                                  "page": 0,
                                                  "size": 1,
                                                  "sortBy": "agreementDate",
                                                  "sortDirection": "DESC"
                                                }
                                            """
                                    )
                            }
                    )
            )
            @RequestBody DealSearchDTO request) {
        log.info("Received request to search deals: {}", request);
        Page<DealDTO> deals = dealService.searchDeals(request);
        return ResponseEntity.ok(deals);
    }

    @Operation(summary = "Поиск сделок с пагинацией и фильтрами")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделки сохранены в Excel файл",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Найденная страница со сделками",
                                    value = """
                                        {
                                          "filePath": "/path/to/exported/deals.xlsx"
                                        }
                                    """

                            )
                    )
            )
    })
    @PostMapping("/search/export")
    public ResponseEntity<String> exportDeals(
            @Parameter(description = "Фильтр поиска",
                    schema = @Schema(implementation = DealSearchDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Фильтр поиска сделок",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealSearchDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Фильтр поиска",
                                            value = """
                                                {
                                                  "description": "description",
                                                  "page": 0,
                                                  "size": 1,
                                                  "sortBy": "agreementDate",
                                                  "sortDirection": "DESC"
                                                }
                                            """
                                    )
                            }
                    )
            )
            @RequestBody DealSearchDTO request) throws IOException {
        log.info("Received request to export deals: {}", request);
        String filePath = excelService.exportDealsToExcel(request);
        return ResponseEntity.ok(filePath);

    }

}
