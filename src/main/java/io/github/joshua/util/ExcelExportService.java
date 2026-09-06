package io.github.joshua.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ExcelExportService {

    public static void exportToExcel(ResultSet rs, String filePath) throws SQLException, IOException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Resultado");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnCount; i++) {
                headerRow.createCell(i).setCellValue(meta.getColumnName(i + 1));
            }

            int rowIndex = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < columnCount; i++) {
                    String value = rs.getString(i + 1);
                    row.createCell(i).setCellValue(value != null ? value : "");
                }
            }

            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Archivo Excel generado: " + filePath);
        }
    }
}