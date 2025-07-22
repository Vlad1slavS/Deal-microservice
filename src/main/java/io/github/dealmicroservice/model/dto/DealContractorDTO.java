package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO для представления контрагента сделки")
public class DealContractorDTO {

    @Schema(description = "Идентификатор контрагента сделки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotNull
    @JsonProperty("deal_id")
    @Schema(description = "Идентификатор сделки", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID dealId;

    @JsonProperty("contractor_id")
    @Schema(description = "Идентификатор контрагента", example = "CONTRACTOR-001")
    private String contractorId;

    @Schema(description = "Имя контрагента", example = "ООО FPI Bank")
    private String name;

    @Schema(description = "ИНН контрагента", example = "7701234567")
    private String inn;

    @Schema(description = "Признак основного контрагента", example = "true")
    private Boolean main;

    @Schema(description = "Список ролей контрагента")
    private List<RoleDTO> roles;

}
