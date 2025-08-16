package io.github.dealmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.controller.v1.DealContractorController;
import io.github.dealmicroservice.exception.GlobalExceptionHandler;
import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.service.DealContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DealContractorControllerTest {

    private DealContractorDTO contractorDTO;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DealContractorService dealContractorService;

    @InjectMocks
    private DealContractorController dealContractorController;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(dealContractorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        contractorDTO = DealContractorDTO.builder()
                .id(UUID.randomUUID())
                .dealId(UUID.randomUUID())
                .main(true)
                .name("Test Contractor")
                .inn("1234567890")
                .build();


    }

    @Test
    void saveContractor_shouldReturnSavedContractorDTO() throws Exception {

        when(dealContractorService.saveDealContractor(any(DealContractorDTO.class))).thenReturn(contractorDTO);

        mockMvc.perform(put("/api/v1/deal-contractor/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractorDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractorDTO.getId().toString()))
                .andExpect(jsonPath("$.deal_id").value(contractorDTO.getDealId().toString()))
                .andExpect(jsonPath("$.name").value(contractorDTO.getName()))
                .andExpect(jsonPath("$.inn").value(contractorDTO.getInn()));

        verify(dealContractorService, times(1)).saveDealContractor(any(DealContractorDTO.class));
    }

    @Test
    void deleteContractor_shouldReturnDeletedContractorDTO() throws Exception {

        DealContractorDTO deletedContractor = DealContractorDTO.builder()
                .id(contractorDTO.getId())
                .dealId(contractorDTO.getDealId())
                .name(contractorDTO.getName())
                .inn(contractorDTO.getInn())
                .build();

        when(dealContractorService.saveDealContractor(any(DealContractorDTO.class))).thenReturn(deletedContractor);

        mockMvc.perform(put("/api/v1/deal-contractor/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractorDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractorDTO.getId().toString()))
                .andExpect(jsonPath("$.deal_id").value(contractorDTO.getDealId().toString()))
                .andExpect(jsonPath("$.name").value(contractorDTO.getName()))
                .andExpect(jsonPath("$.inn").value(contractorDTO.getInn()));

        verify(dealContractorService, times(1)).saveDealContractor(any(DealContractorDTO.class));
    }

}
