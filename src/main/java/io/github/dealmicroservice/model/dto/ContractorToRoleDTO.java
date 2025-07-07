package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ContractorToRoleDTO {

    @NotNull(message = "ID контрагента обязателен")
    @JsonProperty("contractor_id")
    private UUID contractorId;

    @NotNull(message = "ID роли обязателен")
    @NotBlank(message = "ID роли не может быть пустым")
    @JsonProperty("role_id")
    private String roleId;

}
