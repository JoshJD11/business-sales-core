package io.github.joshua.util;

public class AppSettings {
    private static boolean exportSelectsToExcel = false;

    private AppSettings() {}

    public static boolean isExportSelectsToExcel () {
        return exportSelectsToExcel;
    }

    public static void setExportSelectsToExcel(boolean value) {
        exportSelectsToExcel = value;
        System.out.println("Exportación automática a Excel: " + (value ? "ACTIVADA" : "DESACTIVADA"));
    }

}
