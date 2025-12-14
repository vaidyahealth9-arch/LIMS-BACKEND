package com.halo.lims.service;

import com.halo.lims.model.Patient;
import com.halo.lims.repository.PatientRepository;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    private final PatientRepository patientRepository;

    public ExcelService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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
        String[] columns = {"ID", "First Name", "Last Name", "Gender", "Date of Birth", "Phone", "Email"};

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

                row.createCell(0).setCellValue(patient.getId());
                row.createCell(1).setCellValue(patient.getFirstName());
                row.createCell(2).setCellValue(patient.getLastName());
                row.createCell(3).setCellValue(patient.getGender());
                if (patient.getDateOfBirth() != null) {
                    row.createCell(4).setCellValue(patient.getDateOfBirth().toString());
                }
                row.createCell(5).setCellValue(patient.getContactPhone());
                row.createCell(6).setCellValue(patient.getContactEmail());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
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
