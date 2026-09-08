package io.github.joshua.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ExcelExportService {

    private static final String EXPORT_DIR = "exports";

    public static void exportToExcel(ResultSet rs, String fileName) throws SQLException, IOException {
        ResultSetMetaData meta = rs.getMetaData();
        List<String> columns = new java.util.ArrayList<>();
        for (int index = 1; index <= meta.getColumnCount(); index++) {
            columns.add(meta.getColumnLabel(index));
        }
        List<List<String>> rows = new java.util.ArrayList<>();
        while (rs.next()) {
            List<String> row = new java.util.ArrayList<>();
            for (int index = 1; index <= meta.getColumnCount(); index++) {
                row.add(rs.getString(index));
            }
            rows.add(row);
        }
        exportToExcel(new QueryResult(columns, rows), fileName);
    }

    public static void exportToExcel(QueryResult result, String fileName) throws IOException {
        File dir = new File(EXPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de exportaciones: " + EXPORT_DIR);
        }

        String filePath = EXPORT_DIR + "/" + fileName;
        List<String> columns = result.columns();

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("Resultado");
            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < columns.size(); index++) {
                headerRow.createCell(index).setCellValue(columns.get(index));
            }

            int rowIndex = 1;
            for (List<String> values : result.rows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int index = 0; index < values.size(); index++) {
                    row.createCell(index).setCellValue(values.get(index) == null ? "" : values.get(index));
                }
            }

            for (int index = 0; index < columns.size(); index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(fileOut);
        }

        System.out.println("Archivo Excel generado: " + filePath);
    }
}