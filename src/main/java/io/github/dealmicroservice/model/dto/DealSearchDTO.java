package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для поиска (фильтрации) сделок")
public class DealSearchDTO {

    @Schema(description = "Идентификатор сделки", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("deal_id")
    private UUID dealId;

    @Schema(description = "Описание сделки", example = "Поставка оборудования")
    private String description;

    @Schema(description = "Номер соглашения", example = "AG-2024-001")
    @JsonProperty("agreement_number")
    private String agreementNumber;

    @Schema(description = "Дата соглашения (от)", example = "2024-01-01")
    @JsonProperty("agreement_date_from")
    private LocalDate agreementDateFrom;

    @Schema(description = "Дата соглашения (до)", example = "2024-12-31")
    @JsonProperty("agreement_date_to")
    private LocalDate agreementDateTo;

    @Schema(description = "Дата действительности (от)", example = "2024-01-01")
    @JsonProperty("availability_date_from")
    private LocalDate availabilityDateFrom;

    @Schema(description = "Дата действительности (до)", example = "2024-12-31")
    @JsonProperty("availability_date_to")
    private LocalDate availabilityDateTo;

    @Schema(description = "Список типов сделок", example = "['CREDIT', 'OVERDRAFT']")
    private List<String> type;

    @Schema(description = "Список статусов сделок", example = "['ACTIVE', 'CLOSED']")
    private List<String> status;

    @Schema(description = "Дата закрытия (от)", example = "2024-01-01T00:00:00")
    @JsonProperty("close_dt_from")
    private LocalDateTime closeDtFrom;

    @Schema(description = "Дата закрытия (до)", example = "2024-12-31T23:59:59")
    @JsonProperty("close_dt_to")
    private LocalDateTime closeDtTo;

    @Schema(description = "Поиск по заемщику", example = "ООО FPI Bank")
    @JsonProperty("borrower_search")
    private String borrowerSearch;

    @Schema(description = "Поиск по поручителю", example = "ИП Иванов")
    @JsonProperty("warranty_search")
    private String warrantySearch;

    @Schema(description = "Параметры суммы сделки")
    private DealSumDTO sum;

    @Schema(description = "Номер страницы (начиная с 0)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;

    @Schema(description = "Размер страницы", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Размер страницы должен быть больше 0")
    @Max(value = 100, message = "Размер страницы не может быть больше 100")
    private Integer size = 10;

    @Schema(description = "Поле для сортировки", example = "id", defaultValue = "id",
            allowableValues = {"id", "description", "agreementNumber", "agreementDate", "availabilityDate", "type", "status", "closeDt"})
    @Pattern(regexp = "^(id|description|agreementNumber|agreementDate|availabilityDate|type|status|closeDt)$",
            message = "Недопустимое поле для сортировки")
    private String sortBy = "id";

    @Schema(description = "Направление сортировки", example = "ASC", defaultValue = "ASC",
            allowableValues = {"ASC", "DESC"})
    @Pattern(regexp = "^(ASC|DESC)$", message = "Направление сортировки должно быть ASC или DESC")
    private String sortDirection = "ASC";

}
