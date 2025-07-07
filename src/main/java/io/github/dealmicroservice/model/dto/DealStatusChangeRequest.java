package io.github.dealmicroservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealStatusChangeRequest {

    @NotNull
    private UUID id;

    @NotNull
    @NotBlank
    private String statusId;

}
