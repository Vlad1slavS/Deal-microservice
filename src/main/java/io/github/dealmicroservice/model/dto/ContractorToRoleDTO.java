package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO для связи контрагента с ролью")
public class ContractorToRoleDTO {

    @NotNull(message = "ID контрагента обязателен")
    @JsonProperty("contractor_id")
    @Schema(description = "Идентификатор контрагента", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID contractorId;

    @NotNull(message = "ID роли обязателен")
    @NotBlank(message = "ID роли не может быть пустым")
    @JsonProperty("role_id")
    @Schema(description = "Идентификатор роли", example = "GARANT")
    private String roleId;

}