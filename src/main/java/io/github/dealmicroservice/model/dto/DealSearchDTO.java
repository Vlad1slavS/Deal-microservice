package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealSearchDTO {

    @JsonProperty("deal_id")
    private UUID dealId;

    private String description;

    @JsonProperty("agreement_number")
    private String agreementNumber;

    @JsonProperty("agreement_date_from")
    private LocalDate agreementDateFrom;

    @JsonProperty("agreement_date_to")
    private LocalDate agreementDateTo;

    @JsonProperty("availability_date_from")
    private LocalDate availabilityDateFrom;

    @JsonProperty("availability_date_to")
    private LocalDate availabilityDateTo;

    private List<String> type;

    private List<String> status;

    @JsonProperty("close_dt_from")
    private LocalDateTime closeDtFrom;

    @JsonProperty("close_dt_to")
    private LocalDateTime closeDtTo;

    @JsonProperty("borrower_search")
    private String borrowerSearch;

    @JsonProperty("warranty_search")
    private String warrantySearch;

    private DealSumDTO sum;

    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;

    @Min(value = 1, message = "Размер страницы должен быть больше 0")
    @Max(value = 100, message = "Размер страницы не может быть больше 100")
    private Integer size = 20;

    @Pattern(regexp = "^(id|description|agreementNumber|agreementDate|availabilityDate|type|status|closeDt)$",
            message = "Недопустимое поле для сортировки")
    private String sortBy = "id";

    @Pattern(regexp = "^(ASC|DESC)$", message = "Направление сортировки должно быть ASC или DESC")
    private String sortDirection = "ASC";
}
