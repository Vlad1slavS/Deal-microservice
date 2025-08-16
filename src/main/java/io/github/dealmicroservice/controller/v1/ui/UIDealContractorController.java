package io.github.dealmicroservice.controller.v1.ui;

import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.service.DealContractorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/ui/deal-contractor")
@Tag(name = "UI DealContractor", description = "Защищенное API для работы с контрагентами сделок")
public class UIDealContractorController {

    private final Logger log = LogManager.getLogger(UIDealContractorController.class);

    private final DealContractorService dealContractorService;

    public UIDealContractorController(DealContractorService dealContractorService) {
        this.dealContractorService = dealContractorService;
    }

    @Operation(summary = "Создать или обновить контрагента",
            description = """
                    Создание или обновление контрагента с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может создавать/обновлять контрагента
                    - **SUPERUSER** - может создавать/обновлять контрагента
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контрагент создан или обновлен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealContractorDTO.class),
                            examples =
                                    @ExampleObject(
                                            name = "Контрагент создан или обновлен",
                                            value = """
                                                {
                                                    "id": "8f628a7e-8e1f-4bcd-9a02-0c255835b824",
                                                    "name": "ПАО Банк Финансы",
                                                    "inn": "7701234567",
                                                    "main": false,
                                                    "roles": null,
                                                    "deal_id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                    "contractor_id": "CONTR004"
                                                }
                                            """
                                    )
                    )

            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос, ошибка валидации",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Ошибка валидации",
                                        "message": "Поле 'name' не может быть пустым"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован, требуется JWT токен",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Unauthorized",
                                        "message": "JWT token is required"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка/контрагент не найдены",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal not found with id: 999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет прав для создания или обновления контрагента",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Access denied",
                                    }
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('DEAL_SUPERUSER', 'SUPERUSER')")
    @PutMapping("/save")
    public ResponseEntity<DealContractorDTO> saveContractor(
            @Parameter(description = "DTO для создания или обновления контрагента",
                    schema = @Schema(implementation = DealContractorDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealContractorDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Пример для создания контрагента",
                                            value = """
                                                {
                                                    "deal_id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                    "contractor_id": "CONTR004",
                                                    "name": "ПАО Банк Финансы",
                                                    "inn": "7701234567",
                                                    "main": false
                                                }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Пример для обновления сделки",
                                            value = """
                                                {
                                                    "contractor_id": "CONTR004",
                                                    "name": "ПАО Банк Финансы",
                                                    "inn": "7701234567",
                                                    "main": false
                                                }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody DealContractorDTO request) {
        log.info("Received request to save deal contractor: {}", request);
        DealContractorDTO savedContractor = dealContractorService.saveDealContractor(request);
        return ResponseEntity.ok(savedContractor);
    }

    @Operation(
            summary = "Удалить контрагента сделки",
            description = """
                    Удаление контрагента сделки по идентификатору.
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может удалять контрагентов сделок
                    - **SUPERUSER** - может удалять контрагентов сделок
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контрагент сделки успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Контрагент сделки не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal contractor not found with id: 999",
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не авторизован - требуется JWT токен",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Ошибка авторизации",
                                    value = """
                                    {
                                        "error": "Unauthorized",
                                        "message": "JWT token is required"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав для удаления контрагента",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Недостаточно прав",
                                    value = """
                                    {
                                        "error": "Access Denied",
                                    }
                                    """
                            )
                    )
            )

    })
    @PreAuthorize("hasAnyRole('DEAL_SUPERUSER', 'SUPERUSER')")
    @DeleteMapping("/deal-contractor/delete")
    public ResponseEntity<Void> deleteContractor(@RequestParam UUID contractorId) {
        log.info("Received request to delete deal contractor with id: {}", contractorId);
        dealContractorService.deleteDealContractor(contractorId);
        return ResponseEntity.ok().build();
    }

}
