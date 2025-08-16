package io.github.dealmicroservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dealmicroservice.controller.v1.DealController;
import io.github.dealmicroservice.exception.GlobalExceptionHandler;
import io.github.dealmicroservice.model.dto.*;
import io.github.dealmicroservice.service.DealService;
import io.github.dealmicroservice.service.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DealControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DealService dealService;

    @Mock
    private ExcelService excelService;

    @InjectMocks
    private DealController dealController;

    private UUID dealId;
    private DealSaveDTO dealSaveDTO;
    private DealDTO dealDTO;
    private DealStatusChangeRequest statusChangeRequest;
    private DealSearchDTO dealSearchDTO;
    private DealSumDTO sumDTO;
    private DealStatusDTO statusDTO;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        dealId = UUID.randomUUID();

        sumDTO = new DealSumDTO();
        sumDTO.setValue(new BigDecimal("1000.0"));
        sumDTO.setCurrency("USD");

        statusDTO = DealStatusDTO.builder()
                .id("CLOSED")
                .name("Закрытая")
                .build();

        dealSaveDTO = new DealSaveDTO();
        dealSaveDTO.setDescription("Test Description");
        dealSaveDTO.setAgreementNumber("AG-123");

        dealDTO = new DealDTO();
        dealDTO.setId(dealId);
        dealDTO.setDescription("Test Description");
        dealDTO.setSum(sumDTO);
        dealDTO.setStatus(statusDTO);

        statusChangeRequest = new DealStatusChangeRequest();
        statusChangeRequest.setId(dealId);
        statusChangeRequest.setStatusId("CLOSED");

        dealSearchDTO = new DealSearchDTO();
        dealSearchDTO.setPage(0);
        dealSearchDTO.setSize(10);
    }

    @Test
    void saveDeal_Success() throws Exception {
        when(dealService.saveDeal(any(DealSaveDTO.class))).thenReturn(dealDTO);

        mockMvc.perform(put("/api/v1/deal/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealSaveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealDTO.getId().toString()))
                .andExpect(jsonPath("$.description").value(dealDTO.getDescription()))
                .andExpect(jsonPath("$.sum.value").value(dealDTO.getSum().getValue().toString()))
                .andExpect(jsonPath("$.sum.currency").value(dealDTO.getSum().getCurrency()));

        verify(dealService, times(1)).saveDeal(any(DealSaveDTO.class));
    }

    @Test
    void changeStatus_Success() throws Exception {

        DealStatusDTO status = DealStatusDTO.builder()
                .id("CLOSED")
                .name("Закрытая")
                .build();

        DealDTO updatedDeal = new DealDTO();
        updatedDeal.setId(dealId);
        updatedDeal.setStatus(status);

        when(dealService.changeStatus(any(UUID.class), any(String.class))).thenReturn(updatedDeal);

        mockMvc.perform(patch("/api/v1/deal/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealId.toString()))
                .andExpect(jsonPath("$.status.id").value("CLOSED"))
                .andExpect(jsonPath("$.status.name").value("Закрытая"));

        verify(dealService, times(1)).changeStatus(any(UUID.class), any(String.class));

    }

    @Test
    void changeStatus_EmptyBody_BadRequest() throws Exception {

        DealStatusChangeRequest request = new DealStatusChangeRequest();
        request.setId(dealId);
        request.setStatusId(null);

        mockMvc.perform(patch("/api/v1/deal/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dealService, never()).changeStatus(any(UUID.class), any(String.class));
    }

    @Test
    void getDealById_Success() throws Exception {
        when(dealService.getDealById(eq(dealId))).thenReturn(dealDTO);

        mockMvc.perform(get("/api/v1/deal/{id}", dealId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealId.toString()))
                .andExpect(jsonPath("$.description").value(dealDTO.getDescription()))
                .andExpect(jsonPath("$.sum.value").value(dealDTO.getSum().getValue().toString()))
                .andExpect(jsonPath("$.sum.currency").value(dealDTO.getSum().getCurrency()))
                .andExpect(jsonPath("$.status.id").value("CLOSED"))
                .andExpect(jsonPath("$.status.name").value("Закрытая"));

        verify(dealService, times(1)).getDealById(eq(dealId));
    }

    @Test
    void searchDeals_Success() throws Exception {

        List<DealDTO> deals = Arrays.asList(dealDTO);
        Page<DealDTO> dealPage = new PageImpl<>(deals, PageRequest.of(0, 10), 1);

        when(dealService.searchDeals(any(DealSearchDTO.class))).thenReturn(dealPage);

        mockMvc.perform(post("/api/v1/deal/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealSearchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(dealId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(dealService, times(1)).searchDeals(any(DealSearchDTO.class));
    }

    @Test
    void searchDeals_EmptyResult() throws Exception {
        Page<DealDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(dealService.searchDeals(any(DealSearchDTO.class))).thenReturn(emptyPage);

        mockMvc.perform(post("/api/v1/deal/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealSearchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(dealService, times(1)).searchDeals(any(DealSearchDTO.class));
    }

    @Test
    void exportDeals_Success() throws Exception {
        String filePath = "/resources/deals_export.xlsx";

        when(excelService.exportDealsToExcel(any(DealSearchDTO.class))).thenReturn(filePath);

        mockMvc.perform(post("/api/v1/deal/search/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealSearchDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(filePath));

        verify(excelService, times(1)).exportDealsToExcel(any(DealSearchDTO.class));
    }

}
