package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO сделки с основной информацией")
public class DealDTO {

    public interface Create {}
    public interface Update {}

    @Schema(description = "Уникальный идентификатор сделки (обязателен при обновлении)", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(groups = Update.class)
    private UUID id;

    @Schema(description = "Описание сделки (обязательное поле)", example = "Поставка оборудования")
    private String description;

    @Schema(description = "Номер соглашения", example = "AG-2024-001")
    @JsonProperty("agreement_number")
    private String agreementNumber;

    @Schema(description = "Дата соглашения", example = "2024-03-20")
    @JsonProperty("agreement_date")
    private LocalDate agreementDate;

    @Schema(description = "Дата и время начала действия соглашения", example = "2024-03-20T10:00:00")
    @JsonProperty("agreement_start_date")
    private LocalDateTime agreementStartDate;

    @Schema(description = "Дата действия сделки", example = "2024-04-01")
    @JsonProperty("availability_date")
    private LocalDate availabilityDate;

    @Schema(description = "Дата и время закрытия сделки", example = "2024-12-31T23:59:59")
    @JsonProperty("close_dt")
    private LocalDateTime closeDt;

    @Schema(description = "Тип сделки")
    @Transient
    private DealTypeDTO type;

    @Schema(description = "Статус сделки")
    @Transient
    private DealStatusDTO status;

    @Schema(description = "Сумма сделки")
    @Transient
    private DealSumDTO sum;

    @Schema(description = "Список контрагентов сделки")
    @Transient
    private List<DealContractorDTO> contractors;
}
