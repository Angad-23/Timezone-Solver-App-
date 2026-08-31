package com.personal.esttimeconverter.session;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class SessionExcelExporter {

    private static final String[] HEADERS = {
            "learner_email", "learner_name", "tutor_email", "tutor_name",
            "lesson_starttime", "lesson_endtime", "teaching_subject_identifier"
    };

    private SessionExcelExporter() {
    }

    public static byte[] export(List<SessionRow> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Upload");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (SessionRow r : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getLearnerEmail());
                row.createCell(1).setCellValue(r.getLearnerName());
                row.createCell(2).setCellValue(r.getTutorEmail());
                row.createCell(3).setCellValue(r.getTutorName());
                row.createCell(4).setCellValue(r.getLessonStartTime());
                row.createCell(5).setCellValue(r.getLessonEndTime());
                row.createCell(6).setCellValue(r.getTeachingSubjectIdentifier());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
