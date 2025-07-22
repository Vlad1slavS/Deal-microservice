package io.github.dealmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO статуса сделки")
public class DealStatusDTO {

    @Schema(description = "Идентификатор статуса", example = "ACTIVE")
    private String id;

    @Schema(description = "Наименование статуса", example = "Активная")
    private String name;

}
