package io.github.dealmicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractorMessageDTO {

    private String id;

    private String inn;

    private String name;

    @JsonProperty("modify_date")
    private LocalDateTime modifyDate;

}
