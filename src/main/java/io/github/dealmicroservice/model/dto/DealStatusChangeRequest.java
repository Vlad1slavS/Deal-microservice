package io.github.dealmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на изменение статуса сделки")
public class DealStatusChangeRequest {

    @NotNull(message = "ID сделки обязателен")
    @Schema(description = "Идентификатор сделки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotNull(message = "ID статуса обязателен")
    @NotBlank(message = "ID статуса не может быть пустым")
    @Schema(description = "Идентификатор нового статуса", example = "ACTIVE")
    private String statusId;

}