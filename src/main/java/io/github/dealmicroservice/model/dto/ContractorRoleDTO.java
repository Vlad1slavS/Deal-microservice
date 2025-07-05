package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorRoleDTO {
    @NotNull(message = "ID контрагента обязателен")
    @JsonProperty("contractor_id")
    private UUID contractorId;

    @NotNull(message = "ID роли обязателен")
    @JsonProperty("role_id")
    private String roleId;
}
