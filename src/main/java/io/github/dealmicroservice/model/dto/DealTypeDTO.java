package io.github.dealmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO типа сделки")
public class DealTypeDTO {

    @NotNull(message = "ID типа сделки обязателен")
    @Schema(description = "Идентификатор типа сделки", example = "CREDIT")
    private String id;

    @Schema(description = "Наименование типа сделки", example = "Кредитная сделка")
    private String name;

}
