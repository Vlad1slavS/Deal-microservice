package io.github.dealmicroservice.controller.v1.ui;

import io.github.dealmicroservice.model.dto.ContractorToRoleDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/ui/contractor-to-role")
@Slf4j
@Tag(name = "UI ContractorToRole", description = " Защищенный API для работы с ролями контрагентов сделок")
public class UIContractorToRoleController {

    private final DealContractorService dealContractorService;

    public UIContractorToRoleController(DealContractorService dealContractorService) {
        this.dealContractorService = dealContractorService;
    }

    @Operation(
            summary = "Добавить роль контрагенту сделки",
            description = """
                    Добавить роль контрагенту сделки с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может добавлять роли контрагентам сделок
                    - **SUPERUSER** - может добавлять роли контрагентам сделок
                    
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль успешно добавлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorToRoleDTO.class),
                            examples = @ExampleObject(
                                    name = "Роль добавлена",
                                    value = """
                                            {
                                                "contractor_id": "CONTR004",
                                                "role_id": "ROLE001"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное создание роли",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "contractor_id": "CONTR004",
                                        "role_id": "ROLE001"
                                    }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Ошибка валидации",
                                        "message": "ID контрагента обязателен"
                                    }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Контрагент или роль не найдены",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal contractor/role not found with id:"
                                    }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет прав для добавления роли контрагенту сделки",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Нет прав доступа",
                                        "message": "Access denied"
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
    })
    @PostMapping("/add")
    public ResponseEntity<ContractorToRoleDTO> addContractorRole(
            @Parameter(
                    description = "DTO для добавления роли контрагенту сделки",
                    required = true,
                    schema = @Schema(implementation = ContractorToRoleDTO.class),
                    examples = @ExampleObject(value = """
                            {
                                "contractor_id": "CONTR004",
                                "role_id": "ROLE001"
                            }
                            """
                    )
            )
            @Valid @RequestBody ContractorToRoleDTO request) {
        log.info("Received request to add contractor role: {}", request);
        ContractorToRoleDTO savedContractor = dealContractorService.addRoleToContractor(request.getContractorId(), request.getRoleId());
        return ResponseEntity.ok(savedContractor);

    }

    @Operation(summary = "Удалить роль у контрагента сделки",
    security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                    Удалить роль у контрагента сделки с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может удалять роли контрагентам сделок
                    - **SUPERUSER** - может удалять роли контрагентам сделок
                    
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль удалена у контрагента сделки"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Роль не найдена у контрагента сделки",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Contractor role assignment not found",
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет прав для удаления роли контрагенту сделки",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "error": "Нет прав доступа",
                                        "message": "Access denied"
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
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteContractorRole(
            @Parameter(
                    description = "DTO для удаления роли контрагенту сделки",
                    required = true,
                    schema = @Schema(implementation = ContractorToRoleDTO.class),
                    examples = @ExampleObject(value = """
                            {
                                "contractor_id": "CONTR004",
                                "role_id": "ROLE001"
                            }
                            """
                    )
            )
            @Valid @RequestBody ContractorToRoleDTO request) {
        log.info("Received request to delete contractor role: {}", request);
        dealContractorService.deleteRoleFromContractor(request.getContractorId(), request.getRoleId());
        return ResponseEntity.ok().build();
    }

}
