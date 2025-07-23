package io.github.dealmicroservice.controller.v1.ui;

import io.github.customsecuritystarter.SecurityUtils;
import io.github.dealmicroservice.controller.v1.DealControllerContract;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealStatusChangeRequest;
import io.github.dealmicroservice.service.DealService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ui/deal")
@Slf4j
@Tag(name = "UI Deals", description = "Защищенное API для работы со сделками")
public class UIDealController implements DealControllerContract {

    private final DealService dealService;
    private final SecurityUtils securityUtils;

    public UIDealController(DealService dealService, SecurityUtils securityUtils) {
        this.dealService = dealService;
        this.securityUtils = securityUtils;
    }

    @Operation(
            summary = "Создать или обновить сделку",
            description = """
                    Создание или обновление сделки с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может создавать/обновлять любые сделки
                    - **SUPERUSER** - может создавать/обновлять любые сделки
                    
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделка успешно создана или обновлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples = @ExampleObject(
                                    name = "Созданная сделка",
                                    value = """
                                    {
                                      "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                      "description": "Кредитная сделка на покупку недвижимости",
                                      "type": {
                                        "id": "CREDIT",
                                        "name": "Кредит"
                                      },
                                      "status": {
                                        "id": "DRAFT",
                                        "name": "Черновик"
                                      },
                                      "sum": {
                                        "value": 500000.00,
                                        "currency": "RUB"
                                      },
                                      "contractors": [],
                                      "agreement_number": "CREDIT001",
                                      "agreement_date": "2025-07-21",
                                      "agreement_start_date": "2025-07-21T10:00:00",
                                      "availability_date": "2025-07-21",
                                      "close_dt": "2026-07-21T10:00:00"
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
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = """
                                    {
                                        "error": "Ошибка валидации",
                                        "message": "Description is required",
                                        "details": ["Описание сделки не может быть пустым"]
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
                    description = "Недостаточно прав - требуется роль DEAL_SUPERUSER или SUPERUSER",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка для обновления не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Сделка не найдена",
                                    value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal not found with id: 7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1"
                                    }
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('DEAL_SUPERUSER', 'SUPERUSER')")
    @Override
    public ResponseEntity<DealDTO> saveDeal(
            @Parameter(description = "Данные для создания или обновления сделки",
                    schema = @Schema(implementation = DealSaveDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сущность для сохранения сделки",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealSaveDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Создание новой сделки",
                                            value = """
                                            {
                                                "description": "Кредитная сделка на покупку автомобиля",
                                                "typeId": "CREDIT",
                                                "agreement_number": "CREDIT002",
                                                "agreement_date": "2025-07-21T10:00:00",
                                                "agreement_start_date": "2025-07-21T10:00:00",
                                                "availability_date": "2025-07-21T10:00:00",
                                                "close_dt": "2025-12-31T23:59:59"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Обновление существующей сделки",
                                            value = """
                                            {
                                                "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                "description": "Обновленное описание сделки",
                                                "typeId": "OVERDRAFT",
                                                "agreement_number": "CREDIT002",
                                                "agreement_date": "2025-07-21T10:00:00",
                                                "agreement_start_date": "2025-07-21T10:00:00",
                                                "availability_date": "2025-07-21T10:00:00",
                                                "close_dt": "2025-12-31T23:59:59"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody DealSaveDTO request) {
        log.info("UI Request to save deal: {}", request);
        DealDTO savedDeal = dealService.saveDeal(request);
        return ResponseEntity.ok(savedDeal);
    }

    @Operation(
            summary = "Изменить статус сделки",
            description = """
                    Изменение статуса сделки с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **DEAL_SUPERUSER** - может изменять статус любых сделок
                    - **SUPERUSER** - может изменять статус любых сделок
                    
                    **Доступные статусы:**
                    - DRAFT - Черновик
                    - ACTIVE - Активная
                    - CLOSED - Закрытая
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус сделки успешно изменен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples = @ExampleObject(
                                    name = "Сделка с обновленным статусом",
                                    value = """
                                    {
                                      "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                      "description": "Кредитная сделка",
                                      "type": {
                                        "id": "CREDIT",
                                        "name": "Кредит"
                                      },
                                      "status": {
                                        "id": "ACTIVE",
                                        "name": "Активная"
                                      },
                                      "sum": {
                                        "value": 1000000.00,
                                        "currency": "RUB"
                                      },
                                      "contractors": [],
                                      "agreement_number": "CREDIT001",
                                      "agreement_date": "2025-07-21",
                                      "agreement_start_date": "2025-07-21T10:00:00",
                                      "availability_date": "2025-07-21",
                                      "close_dt": "2026-07-21T10:00:00"
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
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = """
                                    {
                                        "error": "Ошибка валидации",
                                        "message": "Invalid deal status",
                                        "details": ["Статус сделки не может быть пустым"]
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
                    description = "Недостаточно прав - требуется роль DEAL_SUPERUSER или SUPERUSER",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка или статус не найдены",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Сделка не найдена",
                                            value = """
                                            {
                                                "error": "Сущность не найдена",
                                                "message": "Deal not found with id: 7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Статус не найден",
                                            value = """
                                            {
                                                "error": "Сущность не найдена",
                                                "message": "Deal status not found: INVALID_STATUS"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasAnyRole('DEAL_SUPERUSER', 'SUPERUSER')")
    @Override
    public ResponseEntity<DealDTO> changeStatus(
            @Parameter(description = "Запрос на изменение статуса сделки",
                    schema = @Schema(implementation = DealStatusChangeRequest.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Идентификатор сделки и новый статус",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealStatusChangeRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Активация сделки",
                                            value = """
                                            {
                                                "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                "statusId": "ACTIVE"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Закрытие сделки",
                                            value = """
                                            {
                                                "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                                "statusId": "CLOSED"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody DealStatusChangeRequest request) {
        log.info("UI Request to change deal status: {}", request);
        DealDTO changedDeal = dealService.changeStatus(request.getId(), request.getStatusId());
        return ResponseEntity.ok(changedDeal);
    }

    @Operation(
            summary = "Получить сделку по ID",
            description = """
                    Получение детальной информации о сделке по её идентификатору:
                    
                    **Доступ по ролям:**
                    - **USER** - может просматривать только основную информацию о сделке
                    - **CREDIT_USER** - может просматривать кредитные сделки (type = CREDIT)
                    - **OVERDRAFT_USER** - может просматривать овердрафтные сделки (type = OVERDRAFT)
                    - **DEAL_SUPERUSER** - может просматривать любые сделки
                    - **SUPERUSER** - может просматривать любые сделки
                    
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделка найдена и доступна для просмотра",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
                            examples = @ExampleObject(
                                    name = "Детальная информация о сделке",
                                    value = """
                                    {
                                      "id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                      "description": "Кредитная сделка на покупку недвижимости",
                                      "type": {
                                        "id": "CREDIT",
                                        "name": "Кредит"
                                      },
                                      "status": {
                                        "id": "ACTIVE",
                                        "name": "Активная"
                                      },
                                      "sum": {
                                        "value": 5000000.00,
                                        "currency": "RUB"
                                      },
                                      "contractors": [
                                        {
                                          "id": "73594325-a36a-4e14-9a05-0155b200a3c3",
                                          "name": "ПАО Банк Финансы",
                                          "inn": "7707083893",
                                          "main": true,
                                          "roles": [
                                            {
                                              "id": "BORROWER",
                                              "name": "Заемщик",
                                              "category": "BORROWER"
                                            }
                                          ],
                                          "deal_id": "7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1",
                                          "contractor_id": "CONTR004"
                                        }
                                      ],
                                      "agreement_number": "CREDIT001",
                                      "agreement_date": "2025-07-21",
                                      "agreement_start_date": "2025-07-21T10:00:00",
                                      "availability_date": "2025-07-21",
                                      "close_dt": "2026-07-21T10:00:00"
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
                    description = "Недостаточно прав для просмотра данного типа сделки",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сделка не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Сделка не найдена",
                                    value = """
                                    {
                                        "error": "Сущность не найдена",
                                        "message": "Deal not found with id: 7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1"
                                    }
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'CREDIT_USER', 'OVERDRAFT_USER', 'DEAL_SUPERUSER', 'SUPERUSER')")
    @Override
    public ResponseEntity<DealDTO> getDealById(
            @PathVariable UUID id) {

        DealDTO deal = dealService.getDealById(id);

        boolean hasAccess = securityUtils.hasDealAccess(deal.getType().getId());

        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(deal);
    }

    @Operation(
            summary = "Поиск сделок с пагинацией и фильтрами",
            description = """
                    Поиск сделок с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **CREDIT_USER** - только кредитные сделки (dealType = CREDIT)
                    - **OVERDRAFT_USER** - только овердрафтные сделки (dealType = OVERDRAFT)
                    - **DEAL_SUPERUSER** - все сделки без ограничений
                    - **SUPERUSER** - все сделки без ограничений
                    
                    """,
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сделки найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDTO.class),
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
                    description = "Недостаточно прав - требуется одна из ролей: CREDIT_USER, OVERDRAFT_USER, DEAL_SUPERUSER, SUPERUSER",
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
    @PreAuthorize("hasAnyRole('CREDIT_USER', 'OVERDRAFT_USER', 'DEAL_SUPERUSER', 'SUPERUSER')")
    @Override
    public ResponseEntity<Page<DealDTO>> searchDeals(
            @Parameter(description = "Фильтр поиска (опционально)",
                    schema = @Schema(implementation = DealSearchDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Фильтр сделки с параметрами поиска",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealSearchDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Фильтр поиска сделок",
                                            value = """
                                            {
                                                "dealId": "DEAL-123",
                                                "contractorId": "CONTR-456",
                                                "dealType": "CREDIT",
                                                "status": "ACTIVE",
                                                "currency": "RUB",
                                                "sumFrom": 100000.00,
                                                "sumTo": 1000000.00,
                                                "closeDateFrom": "2024-01-01",
                                                "closeDateTo": "2024-12-31"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Пустой фильтр (все сделки)",
                                            value = "{}"
                                    )
                            }
                    )
            )
            @RequestBody(required = false) DealSearchDTO searchRequest) {

        if (searchRequest == null) {
            searchRequest = new DealSearchDTO();
        }

        boolean isCreditUser = securityUtils.hasAuthority("ROLE_CREDIT_USER");
        boolean isOverdraftUser = securityUtils.hasAuthority("ROLE_OVERDRAFT_USER");
        boolean isDealSuperUser = securityUtils.hasAuthority("ROLE_DEAL_SUPERUSER");
        boolean isSuperUser = securityUtils.hasAuthority("ROLE_SUPERUSER");

        if (!isDealSuperUser && !isSuperUser) {
            if (isCreditUser && !isOverdraftUser) {
                searchRequest.setType(List.of("CREDIT"));
            } else if (isOverdraftUser && !isCreditUser) {
                searchRequest.setType(List.of("OVERDRAFT"));
            }
        }

        Page<DealDTO> result = dealService.searchDeals(searchRequest);
        return ResponseEntity.ok(result);
    }
}