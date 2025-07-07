package io.github.dealmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO суммы сделки")
public class DealSumDTO {

    @Schema(description = "Сумма сделки", example = "1000000.00")
    private BigDecimal value;

    @Schema(description = "Валюта сделки", example = "RUB")
    private String currency;

}
