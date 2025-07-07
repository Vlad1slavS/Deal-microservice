package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DealContractorDTO {

    private UUID id;

    @NotNull
    @JsonProperty("deal_id")
    private UUID dealId;

    @JsonProperty("contractor_id")
    private String contractorId;

    private String name;

    private String inn;

    private Boolean main;

    private List<RoleDTO> roles;

}
