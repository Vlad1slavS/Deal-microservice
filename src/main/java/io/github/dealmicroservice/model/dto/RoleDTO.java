package io.github.dealmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "DTO роли контрагента в сделке")
public class RoleDTO {

    @Schema(description = "Идентификатор роли", example = "GARANT")
    private String id;

    @Schema(description = "Наименование роли", example = "Гарант")
    private String name;

    @Schema(description = "Категория роли", example = "WARRANTY")
    private String category;

}
