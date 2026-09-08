package io.github.joshua.ui;

import io.github.joshua.database.DBConnection;
import io.github.joshua.util.AppSettings;
import io.github.joshua.util.ExcelExportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private static final List<String> TABLES = List.of("Dim_Product", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Inventory", "Fact_Sales", "Fact_MaterialExpenses");
    private static final Map<String, String> KEYS = Map.of("Dim_Product", "product_id", "Dim_Customer", "customer_id", "Dim_Supplier", "supplier_id", "Dim_ExpenseCategory", "category_id", "Inventory", "inventory_id", "Fact_Sales", "sale_id", "Fact_MaterialExpenses", "expense_id");
    private static final Map<String, String> LABELS = Map.of("Dim_Product", "Productos", "Dim_Customer", "Clientes", "Dim_Supplier", "Proveedores", "Dim_ExpenseCategory", "Categorías", "Inventory", "Inventario", "Fact_Sales", "Ventas", "Fact_MaterialExpenses", "Gastos");

    @FXML private Label pageTitle;
    @FXML private Label connectionLabel;
    @FXML private Label productsCount;
    @FXML private Label customersCount;
    @FXML private Label salesCount;
    @FXML private Label stockCount;
    @FXML private Label tableCaption;
    @FXML private Label exportStatus;
    @FXML private TableView<ObservableList<String>> dataTable;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button createButton;
    private String currentTable;
    private TableData currentData;

    @FXML private void initialize() { showDashboard(); }
    @FXML private void showDashboard() { currentTable = null; currentData = null; pageTitle.setText("Resumen del negocio"); tableCaption.setText("Selecciona una sección para administrar sus registros."); dataTable.getItems().clear(); dataTable.getColumns().clear(); setTableActions(false); refreshDashboard(); }
    @FXML private void showProducts() { showTable("Dim_Product"); }
    @FXML private void showCustomers() { showTable("Dim_Customer"); }
    @FXML private void showSuppliers() { showTable("Dim_Supplier"); }
    @FXML private void showCategories() { showTable("Dim_ExpenseCategory"); }
    @FXML private void showInventory() { showTable("Inventory"); }
    @FXML private void showSales() { showTable("Fact_Sales"); }
    @FXML private void showExpenses() { showTable("Fact_MaterialExpenses"); }
    @FXML private void refresh() { if (currentTable == null) showDashboard(); else showTable(currentTable); }

    @FXML private void createRecord() {
        if (currentTable == null) return;
        if (currentTable.equals("Fact_Sales")) runSaleDialog();
        else if (currentTable.equals("Fact_MaterialExpenses")) runExpenseDialog();
        else editRecord(null);
    }

    @FXML private void editSelected() {
        ObservableList<String> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { notifyUser(Alert.AlertType.INFORMATION, "Selecciona un registro", "Selecciona una fila para editarla."); return; }
        editRecord(selected);
    }

    @FXML private void deleteSelected() {
        if (currentTable == null) return;
        ObservableList<String> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { notifyUser(Alert.AlertType.INFORMATION, "Selecciona un registro", "Selecciona una fila para eliminarla."); return; }
        if (!confirm("Eliminar registro", "Esta acción no se puede deshacer. ¿Deseas continuar?")) return;
        String key = KEYS.get(currentTable);
        execute("DELETE FROM " + currentTable + " WHERE " + key + " = ?", List.of(selected.get(currentData.columnNames.indexOf(key))), true);
    }

    @FXML private void exportCurrent() {
        if (currentTable == null) { notifyUser(Alert.AlertType.INFORMATION, "Sin tabla seleccionada", "Abre una sección antes de exportar."); return; }
        runExport("SELECT TOP 250 * FROM " + currentTable);
    }

    @FXML private void toggleAutomaticExport() { AppSettings.setExportSelectsToExcel(!AppSettings.isExportSelectsToExcel()); updateExportStatus(); }

    @FXML private void openSqlConsole() {
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("Consulta SQL personalizada"); dialog.setHeaderText("Ejecuta consultas de lectura o mantenimiento no destructivo");
        TextArea sql = new TextArea(); sql.setPromptText("SELECT TOP 20 * FROM Dim_Product"); sql.setPrefRowCount(8); sql.setPrefColumnCount(70);
        VBox content = new VBox(10, new Label("Consulta SQL"), sql); content.setPadding(new Insets(10)); dialog.getDialogPane().setContent(content);
        ButtonType run = new ButtonType("Ejecutar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(run, ButtonType.CANCEL);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == run) runSql(sql.getText());
    }

    @FXML private void resetDatabase() {
        TextInputDialog dialog = new TextInputDialog(); dialog.setTitle("Vaciar base de datos"); dialog.setHeaderText("Esta acción eliminará todas las tablas y sus datos."); dialog.setContentText("Escribe CONFIRMAR para continuar:");
        dialog.showAndWait().ifPresent(value -> {
            if (!"CONFIRMAR".equals(value)) { notifyUser(Alert.AlertType.WARNING, "Operación cancelada", "La confirmación debe ser exactamente CONFIRMAR."); return; }
            String[] statements = {"DROP TABLE IF EXISTS Fact_Sales", "DROP TABLE IF EXISTS Fact_MaterialExpenses", "DROP TABLE IF EXISTS Inventory", "DROP TABLE IF EXISTS Dim_Customer", "DROP TABLE IF EXISTS Dim_Supplier", "DROP TABLE IF EXISTS Dim_ExpenseCategory", "DROP TABLE IF EXISTS Dim_Product", "DROP TABLE IF EXISTS Dim_Date"};
            Task<Void> task = new Task<>() { protected Void call() throws SQLException { try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) { for (String sql : statements) s.execute(sql); } return null; } };
            task.setOnSucceeded(event -> { notifyUser(Alert.AlertType.INFORMATION, "Base de datos vaciada", "Las tablas fueron eliminadas correctamente."); showDashboard(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
        });
    }

    private void showTable(String table) {
        if (!TABLES.contains(table)) return;
        currentTable = table; pageTitle.setText(LABELS.get(table)); tableCaption.setText(LABELS.get(table) + " | selecciona una fila para editar o eliminar"); setTableActions(true);
        Task<TableData> task = new Task<>() { protected TableData call() throws SQLException { try (Connection c = DBConnection.getConnection(); PreparedStatement s = c.prepareStatement("SELECT TOP 250 * FROM " + table); ResultSet r = s.executeQuery()) { return readTable(r); } } };
        task.setOnSucceeded(event -> { currentData = task.getValue(); dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); setConnected(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void refreshDashboard() {
        Task<long[]> task = new Task<>() { protected long[] call() throws SQLException { try (Connection c = DBConnection.getConnection()) { return new long[]{count(c, "Dim_Product"), count(c, "Dim_Customer"), count(c, "Fact_Sales"), count(c, "Inventory")}; } } };
        task.setOnSucceeded(event -> { long[] v = task.getValue(); productsCount.setText("" + v[0]); customersCount.setText("" + v[1]); salesCount.setText("" + v[2]); stockCount.setText("" + v[3]); setConnected(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void editRecord(ObservableList<String> selected) {
        List<String> columns = currentData == null ? List.of() : currentData.columnNames; List<String> editable = new ArrayList<>();
        for (String column : columns) if (!column.equals(KEYS.get(currentTable))) editable.add(column);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10)); List<TextField> fields = new ArrayList<>();
        for (int i = 0; i < editable.size(); i++) { String column = editable.get(i); TextField field = new TextField(); field.setPromptText(column); if (selected != null) field.setText(selected.get(columns.indexOf(column))); grid.add(new Label(column), 0, i); grid.add(field, 1, i); fields.add(field); }
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(selected == null ? "Nuevo registro" : "Editar registro"); dialog.setHeaderText(LABELS.get(currentTable)); dialog.getDialogPane().setContent(grid); ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != save) return;
        if (selected == null) { String placeholders = String.join(", ", editable.stream().map(value -> "?").toList()); execute("INSERT INTO " + currentTable + " (" + String.join(", ", editable) + ") VALUES (" + placeholders + ")", fields.stream().map(field -> field.getText()).toList(), true); }
        else { String assignments = String.join(", ", editable.stream().map(value -> value + " = ?").toList()); List<String> values = new ArrayList<>(fields.stream().map(field -> field.getText()).toList()); values.add(selected.get(columns.indexOf(KEYS.get(currentTable)))); execute("UPDATE " + currentTable + " SET " + assignments + " WHERE " + KEYS.get(currentTable) + " = ?", values, true); }
    }

    private void runSaleDialog() { GridPane grid = formGrid(List.of("Producto", "Cantidad", "Método de pago", "Correo cliente")); List<TextField> f = fieldsOf(grid); if (showForm("Registrar venta", grid)) execute("{CALL InsertSale(?, ?, ?, ?)}", List.of(f.get(0).getText(), f.get(1).getText(), f.get(2).getText(), f.get(3).getText()), true); }
    private void runExpenseDialog() { GridPane grid = formGrid(List.of("Categoría", "Producto", "Correo proveedor", "Descripción", "Monto", "Método de pago", "Cantidad")); List<TextField> f = fieldsOf(grid); if (showForm("Registrar gasto", grid)) execute("{CALL InsertExpense(?, ?, ?, ?, ?, ?, ?)}", f.stream().map(field -> field.getText()).toList(), true); }

    private void runSql(String sql) {
        String trimmed = sql == null ? "" : sql.trim(); if (trimmed.isEmpty()) return;
        if (trimmed.matches("(?i)^(DROP|TRUNCATE|DELETE)\\b.*")) { notifyUser(Alert.AlertType.WARNING, "Consulta bloqueada", "DROP, TRUNCATE y DELETE requieren usar la opción de limpieza con confirmación."); return; }
        if (trimmed.matches("(?i)^SELECT\\b.*")) runQuery(trimmed); else execute(trimmed, List.of(), false);
    }

    private void runQuery(String sql) {
        Task<TableData> task = new Task<>() { protected TableData call() throws SQLException { try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { return readTable(r); } } };
        task.setOnSucceeded(event -> { currentData = task.getValue(); currentTable = null; dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); pageTitle.setText("Consulta SQL"); tableCaption.setText("Resultado de consulta personalizada"); setConnected(); if (AppSettings.isExportSelectsToExcel()) runExport(sql); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void runExport(String sql) {
        Task<Void> task = new Task<>() { protected Void call() throws Exception { try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { ExcelExportService.exportToExcel(r, "resultado_" + System.currentTimeMillis() + ".xlsx"); } return null; } };
        task.setOnSucceeded(event -> notifyUser(Alert.AlertType.INFORMATION, "Exportación completada", "Archivo generado en la carpeta exports.")); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void execute(String sql, List<?> values, boolean refresh) {
        Task<Integer> task = new Task<>() { protected Integer call() throws SQLException { try (Connection c = DBConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { for (int i = 0; i < values.size(); i++) s.setObject(i + 1, values.get(i)); return s.executeUpdate(); } } };
        task.setOnSucceeded(event -> { notifyUser(Alert.AlertType.INFORMATION, "Operación completada", "La operación se ejecutó correctamente."); if (refresh) showTable(currentTable); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private static long count(Connection c, String table) throws SQLException { try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) { r.next(); return r.getLong(1); } }
    private static TableData readTable(ResultSet result) throws SQLException { ResultSetMetaData meta = result.getMetaData(); List<String> names = new ArrayList<>(); ObservableList<TableColumn<ObservableList<String>, String>> columns = FXCollections.observableArrayList(); for (int i = 1; i <= meta.getColumnCount(); i++) { names.add(meta.getColumnLabel(i)); int index = i - 1; TableColumn<ObservableList<String>, String> column = new TableColumn<>(meta.getColumnLabel(i)); column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(index))); column.setPrefWidth(150); columns.add(column); } ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList(); while (result.next()) { ObservableList<String> row = FXCollections.observableArrayList(); for (int i = 1; i <= meta.getColumnCount(); i++) row.add(result.getString(i)); rows.add(row); } return new TableData(names, columns, rows); }
    private GridPane formGrid(List<String> labels) { GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10)); for (int i = 0; i < labels.size(); i++) { grid.add(new Label(labels.get(i)), 0, i); grid.add(new TextField(), 1, i); } return grid; }
    private List<TextField> fieldsOf(GridPane grid) { return grid.getChildren().stream().filter(node -> node instanceof TextField).map(node -> (TextField) node).toList(); }
    private boolean showForm(String title, GridPane grid) { Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(title); dialog.getDialogPane().setContent(grid); ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL); return dialog.showAndWait().orElse(ButtonType.CANCEL) == save; }
    private void setTableActions(boolean enabled) { createButton.setDisable(!enabled); editButton.setDisable(!enabled); deleteButton.setDisable(!enabled); }
    private void updateExportStatus() { exportStatus.setText(AppSettings.isExportSelectsToExcel() ? "Exportación automática: activa" : "Exportación automática: desactivada"); }
    private void setConnected() { connectionLabel.setText("Base de datos conectada"); connectionLabel.getStyleClass().setAll("connection-ok"); updateExportStatus(); }
    private void start(Task<?> task) { Thread thread = new Thread(task, "business-sales-ui-task"); thread.setDaemon(true); thread.start(); }
    private boolean confirm(String title, String message) { Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK); alert.setTitle(title); return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK; }
    private void showError(Throwable error) { notifyUser(Alert.AlertType.ERROR, "No se pudo completar la operación", error == null ? "Error desconocido" : error.getMessage()); }
    private void notifyUser(Alert.AlertType type, String title, String message) { Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.show(); }
    private record TableData(List<String> columnNames, ObservableList<TableColumn<ObservableList<String>, String>> columns, ObservableList<ObservableList<String>> rows) { }
}
