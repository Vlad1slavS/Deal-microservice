package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DealDTO {

    public interface Create {}
    public interface Update {}

    @NotNull(groups = Update.class)
    private UUID id;

    @NotBlank(groups = {Create.class, Update.class})
    private String description;

    @JsonProperty("agreement_number")
    private String agreementNumber;

    @JsonProperty("agreement_date")
    private LocalDate agreementDate;

    @JsonProperty("agreement_start_dt")
    private LocalDateTime agreementStartDate;

    @JsonProperty("availability_date")
    private LocalDate availabilityDate;

    @JsonProperty("close_dt")
    private LocalDateTime closeDt;

    private List<ContractorDTO> contractors;

    private DealTypeDTO type;

    private DealStatusDTO status;

    private DealSumDTO sum;


}
