package io.github.joshua.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class QueryResultPresenter {

    private QueryResultPresenter() {
    }

    public static void present(ResultSet resultSet) throws SQLException, IOException {
        present(QueryResult.from(resultSet));
    }

    public static void present(QueryResult result) throws IOException {
        if (AppSettings.isExportSelectsToExcel()) {
            ExcelExportService.exportToExcel(result, "resultado_" + System.currentTimeMillis() + ".xlsx");
        } else {
            printTable(result);
        }
    }

    public static void printTable(QueryResult result) {
        int[] widths = new int[result.columns().size()];
        for (int index = 0; index < widths.length; index++) widths[index] = result.columns().get(index).length();
        for (java.util.List<String> row : result.rows()) {
            for (int index = 0; index < widths.length; index++) widths[index] = Math.max(widths[index], row.get(index) == null ? 0 : row.get(index).length());
        }
        String border = border(widths);
        System.out.println(border);
        printRow(result.columns().toArray(String[]::new), widths);
        System.out.println(border);
        for (java.util.List<String> row : result.rows()) printRow(row.toArray(String[]::new), widths);
        System.out.println(border);
    }

    public static void printTable(ResultSet resultSet) throws SQLException {
        printTable(QueryResult.from(resultSet));
    }

    private static void printRow(String[] values, int[] widths) {
        StringBuilder row = new StringBuilder("|");
        for (int index = 0; index < values.length; index++) {
            String value = values[index] == null ? "" : values[index];
            row.append(" ").append(String.format("%-" + widths[index] + "s", value)).append(" |");
        }
        System.out.println(row);
    }

    private static String border(int[] widths) {
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            border.append("-").append("-".repeat(width)).append("-+");
        }
        return border.toString();
    }
}
