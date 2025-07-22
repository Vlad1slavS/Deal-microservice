package io.github.dealmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.exception.GlobalExceptionHandler;
import io.github.dealmicroservice.model.dto.ContractorToRoleDTO;
import io.github.dealmicroservice.service.DealContractorService;
import io.github.dealmicroservice.service.DealContractorServiceImpl;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ContractorToRoleControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DealContractorService dealContractorService;

    @InjectMocks
    private ContractorToRoleController contractorToRoleController;

    private ContractorToRoleDTO contractorToRoleDTO;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(contractorToRoleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        contractorToRoleDTO = ContractorToRoleDTO.builder()
                .contractorId(UUID.randomUUID())
                .roleId("DRAWER")
                .build();

    }

    @Test
    void addContractorToRole_shouldReturnSavedContractorToRoleDTO() throws Exception {

        when(dealContractorService.addRoleToContractor(any(UUID.class), any(String.class))).thenReturn(contractorToRoleDTO);

        mockMvc.perform(post("/api/v1/contractor-to-role/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractorToRoleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractor_id").value(contractorToRoleDTO.getContractorId().toString()))
                .andExpect(jsonPath("$.role_id").value("DRAWER"));

        verify(dealContractorService, times(1)).addRoleToContractor(any(UUID.class), any(String.class));
    }


}
