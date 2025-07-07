package io.github.dealmicroservice.service;

import io.github.dealmicroservice.model.dto.DealContractorDTO;
import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ExcelService {

    private final DealService dealService;

    public ExcelService(DealService dealService) {
        this.dealService = dealService;
    }

    public String exportDealsToExcel(DealSearchDTO request) throws IOException {
        log.info("Exporting deals to Excel with criteria: {}", request);

        request.setSize(10000);
        List<DealDTO> deals = dealService.searchDeals(request).getContent();

        String exportDir = "src/main/resources/export";
        Path exportPath = Paths.get(exportDir);
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
            log.info("Created export directory: {}", exportPath.toAbsolutePath());
        }

        String fileName = "deals_page_export.xlsx";
        Path filePath = exportPath.resolve(fileName);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {

            Sheet sheet = workbook.createSheet("Deals");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Описание", "Номер договора", "Дата договора",
                    "Дата начала", "Дата доступности", "Тип", "Статус",
                    "Сумма", "Валюта", "Дата закрытия", "Основной контрагент"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (int i = 0; i < deals.size(); i++) {
                Row row = sheet.createRow(i + 1);
                DealDTO deal = deals.get(i);

                row.createCell(0).setCellValue(deal.getId().toString());
                row.createCell(1).setCellValue(deal.getDescription());
                row.createCell(2).setCellValue(deal.getAgreementNumber());
                row.createCell(3).setCellValue(deal.getAgreementDate() != null ?
                        deal.getAgreementDate().format(dateFormatter) : "");
                row.createCell(4).setCellValue(deal.getAgreementStartDate() != null ?
                        deal.getAgreementStartDate().format(dateTimeFormatter) : "");
                row.createCell(5).setCellValue(deal.getAvailabilityDate() != null ?
                        deal.getAvailabilityDate().format(dateFormatter) : "");
                row.createCell(6).setCellValue(deal.getType() != null ? deal.getType().getName() : "");
                row.createCell(7).setCellValue(deal.getStatus() != null ? deal.getStatus().getName() : "");
                row.createCell(8).setCellValue(deal.getSum() != null ?
                        deal.getSum().getValue().toString() : "");
                row.createCell(9).setCellValue(deal.getSum() != null ? deal.getSum().getCurrency() : "");
                row.createCell(10).setCellValue(deal.getCloseDt() != null ?
                        deal.getCloseDt().format(dateTimeFormatter) : "");

                String mainContractor = "";
                if (deal.getContractors() != null) {
                    mainContractor = deal.getContractors().stream()
                            .filter(c -> Boolean.TRUE.equals(c.getMain()))
                            .findFirst()
                            .map(DealContractorDTO::getName)
                            .orElse("");
                }
                row.createCell(11).setCellValue(mainContractor);
            }

            workbook.write(fileOut);

            log.info("Excel file successfully saved to: {}", filePath.toAbsolutePath());

            return filePath.toAbsolutePath().toString();
        }
    }

}
