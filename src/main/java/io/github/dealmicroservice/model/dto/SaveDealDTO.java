package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SaveDealDTO {

    private UUID id;

    private String description;

    @JsonProperty("agreement_number")
    private String agreementNumber;

    @JsonProperty("agreement_date")
    private LocalDate agreementDate;

    @JsonProperty("agreement_start_dt")
    private LocalDateTime agreementStartDate;

    @JsonProperty("availability_date")
    private LocalDate availabilityDate;

    @JsonProperty("type_id")
    private String typeId;

    @JsonProperty("close_dt")
    private LocalDateTime closeDt;

}
