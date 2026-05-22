package com.halo.lims.service;

import com.halo.lims.dto.billing.BillResponse;
import com.halo.lims.model.Patient;
import com.halo.lims.model.Bill;
import com.halo.lims.repository.PatientRepository;
import com.halo.lims.repository.BillRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    private final PatientRepository patientRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;

    public ExcelService(PatientRepository patientRepository, BillRepository billRepository, BillingService billingService) {
        this.patientRepository = patientRepository;
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    public void importPatients(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<Patient> patients = new ArrayList<>();

            int rowNumber = 0;
            Map<String, Integer> columnMap = new HashMap<>();
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    for (Cell cell : currentRow) {
                        columnMap.put(cell.getStringCellValue(), cell.getColumnIndex());
                    }
                    rowNumber++;
                    continue;
                }

                try {
                    Patient patient = new Patient();
                    patient.setFirstName(getStringCellValue(currentRow.getCell(columnMap.get("First Name"))));
                    patient.setLastName(getStringCellValue(currentRow.getCell(columnMap.get("Last Name"))));
                    patient.setGender(getStringCellValue(currentRow.getCell(columnMap.get("Gender"))));
                    patient.setDateOfBirth(getLocalDateCellValue(currentRow.getCell(columnMap.get("Date of Birth"))));
                    patient.setContactPhone(getStringCellValue(currentRow.getCell(columnMap.get("Phone"))));
                    patient.setContactEmail(getStringCellValue(currentRow.getCell(columnMap.get("Email"))));

                    patients.add(patient);
                } catch (Exception e) {
                    // Log the error and continue to the next row
                    logger.error("Error processing row " + rowNumber + ": " + e.getMessage());
                }
                rowNumber++;
            }

            patientRepository.saveAll(patients);

            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportPatients(List<Patient> patients) throws IOException {
        String[] columns = {
                "ID",
                "MRN",
                "First Name",
                "Last Name",
                "Gender",
                "Date of Birth",
                "Phone",
                "Email",
                "Tests Done",
                "Invoice Numbers",
                "Bill Statuses",
                "Total Billed",
                "Total Paid",
                "Total Due",
                "Last Payment Method",
                "Last Payment Date"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet("Patients");

            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            int rowIdx = 1;
            for (Patient patient : patients) {
                Row row = sheet.createRow(rowIdx++);
                List<BillResponse> billResponses = getBillResponsesForPatient(patient.getId());
                Set<String> testNames = new LinkedHashSet<>();
                Set<String> invoiceNumbers = new LinkedHashSet<>();
                Set<String> billStatuses = new LinkedHashSet<>();

                java.math.BigDecimal totalBilled = java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalPaid = java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalDue = java.math.BigDecimal.ZERO;
                String lastPaymentMethod = "";
                String lastPaymentDate = "";

                for (BillResponse billResponse : billResponses) {
                    if (billResponse.getInvoiceNumber() != null) {
                        invoiceNumbers.add(billResponse.getInvoiceNumber());
                    }
                    if (billResponse.getStatus() != null) {
                        billStatuses.add(billResponse.getStatus());
                    }
                    totalBilled = totalBilled.add(safeBigDecimal(billResponse.getNetAmount()));
                    totalPaid = totalPaid.add(safeBigDecimal(billResponse.getPaidAmount()));
                    totalDue = totalDue.add(safeBigDecimal(billResponse.getDueAmount()));
                    if (billResponse.getPaymentMethod() != null && !billResponse.getPaymentMethod().isBlank()) {
                        lastPaymentMethod = billResponse.getPaymentMethod();
                    }
                    if (billResponse.getPaymentDate() != null) {
                        lastPaymentDate = billResponse.getPaymentDate().toString();
                    }

                    if (billResponse.getServiceRequests() != null) {
                        billResponse.getServiceRequests().forEach(serviceRequest -> {
                            if (serviceRequest.getRequestedTests() != null) {
                                serviceRequest.getRequestedTests().forEach(test -> {
                                    if (test.getTestName() != null && !test.getTestName().isBlank()) {
                                        testNames.add(test.getTestName());
                                    }
                                });
                            }
                        });
                    }
                }

                row.createCell(0).setCellValue(patient.getId());
                row.createCell(1).setCellValue(patient.getLocalMrnValue());
                row.createCell(2).setCellValue(patient.getFirstName());
                row.createCell(3).setCellValue(patient.getLastName());
                row.createCell(4).setCellValue(patient.getGender());
                if (patient.getDateOfBirth() != null) {
                    row.createCell(5).setCellValue(patient.getDateOfBirth().toString());
                }
                row.createCell(6).setCellValue(patient.getContactPhone());
                row.createCell(7).setCellValue(patient.getContactEmail());
                row.createCell(8).setCellValue(String.join(", ", testNames));
                row.createCell(9).setCellValue(String.join(", ", invoiceNumbers));
                row.createCell(10).setCellValue(String.join(", ", billStatuses));
                row.createCell(11).setCellValue(totalBilled.doubleValue());
                row.createCell(12).setCellValue(totalPaid.doubleValue());
                row.createCell(13).setCellValue(totalDue.doubleValue());
                row.createCell(14).setCellValue(lastPaymentMethod);
                row.createCell(15).setCellValue(lastPaymentDate);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private List<BillResponse> getBillResponsesForPatient(Integer patientId) {
        if (patientId == null) {
            return List.of();
        }

        List<Bill> bills = billRepository.findByPatient_Id(patientId);
        return bills.stream()
                .map(bill -> billingService.getBillById(bill.getId()))
                .collect(Collectors.toList());
    }

    private java.math.BigDecimal safeBigDecimal(java.math.BigDecimal value) {
        return value == null ? java.math.BigDecimal.ZERO : value;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private LocalDate getLocalDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                String dateString = cell.getStringCellValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][MM/dd/yyyy][M/d/yy]");
                return LocalDate.parse(dateString, formatter);
            case NUMERIC:
                return cell.getLocalDateTimeCellValue().toLocalDate();
            default:
                return null;
        }
    }
}
