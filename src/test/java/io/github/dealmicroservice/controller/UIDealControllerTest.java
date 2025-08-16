package io.github.dealmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.customsecuritystarter.SecurityUtils;
import io.github.dealmicroservice.controller.v1.ui.UIDealController;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import io.github.dealmicroservice.model.dto.DealStatusDTO;
import io.github.dealmicroservice.model.dto.DealSumDTO;
import io.github.dealmicroservice.model.dto.DealTypeDTO;
import io.github.dealmicroservice.security.config.SecurityConfig;
import io.github.dealmicroservice.service.DealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UIDealController.class)
@Import({SecurityConfig.class, SecurityUtils.class})
public class UIDealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private final ObjectMapper objectMapper;

    @MockitoBean
    private DealService dealService;

    @MockitoBean
    private SecurityUtils securityUtils;

    private DealDTO testDeal;
    private DealSaveDTO testSaveRequest;
    private UUID testDealId;

    public UIDealControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        testDealId = UUID.fromString("7ab0ec84-7a0d-48bc-aaf5-07ac07661ef1");

        testDeal = DealDTO.builder()
                .id(testDealId)
                .description("Test Deal")
                .type(DealTypeDTO.builder().id("CREDIT").name("Кредит").build())
                .status(DealStatusDTO.builder().id("DRAFT").name("Черновик").build())
                .sum(DealSumDTO.builder().value(BigDecimal.valueOf(1000000)).currency("RUB").build())
                .contractors(Collections.emptyList())
                .agreementNumber("TEST001")
                .agreementDate(LocalDate.now())
                .agreementStartDate(LocalDateTime.now())
                .availabilityDate(LocalDate.now())
                .closeDt(LocalDateTime.now().plusYears(1))
                .build();

        testSaveRequest = DealSaveDTO.builder()
                .description("Test Deal")
                .typeId("CREDIT")
                .agreementNumber("TEST001")
                .agreementStartDate(LocalDateTime.now())
                .closeDt(LocalDateTime.now().plusYears(1))
                .build();

        when(securityUtils.hasAuthority(anyString())).thenReturn(true);
        when(securityUtils.getCurrentUserRoles()).thenReturn(Set.of("ROLE_DEAL_SUPERUSER"));
    }

    @Test
    @WithMockUser(roles = {"DEAL_SUPERUSER"})
    void shouldSaveDealSuccessfully() throws Exception {

        when(dealService.saveDeal(any(DealSaveDTO.class))).thenReturn(testDeal);

        mockMvc.perform(put("/api/v1/ui/deal/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSaveRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testDealId.toString()))
                .andExpect(jsonPath("$.description").value("Test Deal"))
                .andExpect(jsonPath("$.type.id").value("CREDIT"))
                .andExpect(jsonPath("$.status.id").value("DRAFT"));

        verify(dealService).saveDeal(any(DealSaveDTO.class));
    }

}
