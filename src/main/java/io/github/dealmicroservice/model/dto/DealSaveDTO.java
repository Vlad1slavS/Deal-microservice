package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для сохранения сделки")
@Builder
public class DealSaveDTO {

    @Schema(description = "Идентификатор сделки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Описание сделки", example = "Сделка по продаже оборудования")
    private String description;

    @JsonProperty("agreement_number")
    @Schema(description = "Номер соглашения", example = "AG-2023-001")
    private String agreementNumber;

    @JsonProperty("agreement_date")
    @Schema(description = "Дата соглашения", example = "2023-10-01")
    private LocalDate agreementDate;

    @JsonProperty("agreement_start_date")
    @Schema(description = "Дата начала действия соглашения", example = "2023-10-01T10:00:00")
    private LocalDateTime agreementStartDate;

    @JsonProperty("availability_date")
    @Schema(description = "Дата действия сделки", example = "2023-10-15")
    private LocalDate availabilityDate;

    @JsonProperty("type_id")
    @Schema(description = "Идентификатор типа сделки", example = "OTHER")
    private String typeId;

    @JsonProperty("close_dt")
    @Schema(description = "Дата закрытия сделки", example = "2023-12-31T18:00:00")
    private LocalDateTime closeDt;

}
