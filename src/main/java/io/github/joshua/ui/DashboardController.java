package io.github.joshua.ui;

import io.github.joshua.admin.DatabaseResetService;
import io.github.joshua.admin.SqlConsoleService;
import io.github.joshua.costs.BusinessExpense;
import io.github.joshua.customer.CustomerService;
import io.github.joshua.expensecategory.ExpenseCategory;
import io.github.joshua.inventory.InventoryManager;
import io.github.joshua.notification.EmailNotificationSender;
import io.github.joshua.notification.WhatsAppNotificationSender;
import io.github.joshua.product.ProductService;
import io.github.joshua.sales.SalesService;
import io.github.joshua.supplier.SupplierService;
import io.github.joshua.util.AppSettings;
import io.github.joshua.util.QueryResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Map;

public class DashboardController {
    private static final List<String> TABLES = List.of("Dim_Product", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Inventory", "Fact_Sales", "Fact_MaterialExpenses");
    private static final Map<String, String> LABELS = Map.of("Dim_Product", "Productos", "Dim_Customer", "Clientes", "Dim_Supplier", "Proveedores", "Dim_ExpenseCategory", "Categorías", "Inventory", "Inventario", "Fact_Sales", "Ventas", "Fact_MaterialExpenses", "Gastos");

    @FXML private Label pageTitle;
    @FXML private Label connectionLabel;
    @FXML private Label productsCount;
    @FXML private Label customersCount;
    @FXML private Label salesCount;
    @FXML private Label stockCount;
    @FXML private Label tableCaption;
    @FXML private Label exportStatus;
    @FXML private Button notificationButton;
    @FXML private TableView<ObservableList<String>> dataTable;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button createButton;
    @FXML private Button queryButton;
    @FXML private StackPane tableContent;
    @FXML private VBox loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label loadingLabel;
    private String currentTable;
    private TableData currentData;
    private Task<?> activeTask;
    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final SupplierService supplierService = new SupplierService();
    private final SalesService salesService = new SalesService();
    private final BusinessExpense expenseService = new BusinessExpense();
    private final InventoryManager inventoryService = new InventoryManager(new EmailNotificationSender()); // Email notification sender by default
    private final ExpenseCategory categoryService = new ExpenseCategory();
    private final DatabaseResetService resetService = new DatabaseResetService();
    private final SqlConsoleService sqlService = new SqlConsoleService();
    private boolean isNotifierEmail = true;

    @FXML private void initialize() { dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); updateNotificationButton(); showDashboard(); }
    @FXML private void showDashboard() { currentTable = null; currentData = null; pageTitle.setText("Resumen del negocio"); tableCaption.setText("Selecciona una sección para administrar sus registros."); dataTable.getItems().clear(); dataTable.getColumns().clear(); setTableActions(false); refreshDashboard(); }
    @FXML private void showProducts() { showTable("Dim_Product"); }
    @FXML private void showCustomers() { showTable("Dim_Customer"); }
    @FXML private void showSuppliers() { showTable("Dim_Supplier"); }
    @FXML private void showCategories() { showTable("Dim_ExpenseCategory"); }
    @FXML private void showInventory() { showTable("Inventory"); }
    @FXML private void showSales() { showTable("Fact_Sales"); }
    @FXML private void showExpenses() { showTable("Fact_MaterialExpenses"); }
    @FXML private void refresh() { if (currentTable == null) showDashboard(); else showTable(currentTable); }

    @FXML private void filterCurrent() {
        if (currentTable == null) return;
        if (currentTable.equals("Dim_Supplier")) {
            ChoiceDialog<String> mode = centered(new ChoiceDialog<>("Correo", "Correo", "Nombre de producto"));
            mode.setTitle("Filtrar Proveedores");
            mode.setHeaderText("Elige uno de los filtros disponibles en SupplierService");
            mode.setContentText("Buscar por:");
            mode.showAndWait().ifPresent(selectedMode -> showFilterValueDialog(selectedMode));
            return;
        }
        String filterLabel = switch (currentTable) {
            case "Dim_Product" -> "nombre de producto";
            case "Dim_Customer" -> "correo";
            case "Inventory", "Fact_Sales", "Fact_MaterialExpenses" -> "nombre de producto";
            case "Dim_ExpenseCategory" -> "nombre de categoría";
            default -> "filtro";
        };
        TextInputDialog dialog = centered(new TextInputDialog());
        dialog.setTitle("Filtrar " + LABELS.get(currentTable));
        dialog.setHeaderText("Consulta disponible en el servicio de " + LABELS.get(currentTable));
        dialog.setContentText("Escribe el " + filterLabel + ":");
        dialog.showAndWait().ifPresent(value -> loadQuery(currentTable, value));
    }

    private void showFilterValueDialog(String mode) {
        TextInputDialog dialog = centered(new TextInputDialog());
        dialog.setTitle("Filtrar Proveedores");
        dialog.setHeaderText("Consulta disponible en SupplierService");
        dialog.setContentText("Correo".equals(mode) ? "Escribe el correo:" : "Escribe el nombre del producto:");
        dialog.showAndWait().ifPresent(value -> {
            Task<TableData> task = new Task<>() { protected TableData call() throws Exception {
                QueryResult result = "Correo".equals(mode)
                        ? supplierService.querySupplierByEmail(value)
                        : supplierService.querySupplierByProduct(value);
                return readTable(result);
            }};
            task.setOnSucceeded(event -> { currentData = task.getValue(); dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); tableCaption.setText("Proveedores | filtro aplicado"); setConnected(); });
            task.setOnFailed(event -> showError(task.getException()));
            start(task);
        });
    }

    @FXML private void createRecord() {
        if (currentTable == null) return;
        if (currentTable.equals("Fact_Sales")) runSaleDialog(null);
        else if (currentTable.equals("Fact_MaterialExpenses")) runExpenseDialog(null);
        else editRecord(null);
    }

    @FXML private void editSelected() {
        ObservableList<String> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { notifyUser(Alert.AlertType.INFORMATION, "Selecciona un registro", "Selecciona una fila para editarla."); return; }
        if (currentTable.equals("Fact_Sales")) runSaleDialog(selected);
        else if (currentTable.equals("Fact_MaterialExpenses")) runExpenseDialog(selected);
        else editRecord(selected);
    }

    @FXML private void deleteSelected() {
        if (currentTable == null) return;
        ObservableList<String> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) { notifyUser(Alert.AlertType.INFORMATION, "Selecciona un registro", "Selecciona una fila para eliminarla."); return; }
        if (!confirm("Eliminar registro", "Esta acción no se puede deshacer. ¿Deseas continuar?")) return;
        deleteThroughService(selected);
    }

    @FXML private void exportCurrent() {
        if (currentTable == null) { notifyUser(Alert.AlertType.INFORMATION, "Sin tabla seleccionada", "Abre una sección antes de exportar."); return; }
        runExport("SELECT TOP 250 * FROM " + currentTable);
    }

    @FXML private void toggleAutomaticExport() { AppSettings.setExportSelectsToExcel(!AppSettings.isExportSelectsToExcel()); updateExportStatus(); }

    @FXML private void toggleNotificationMethod() {
        inventoryService.setNotificationMethod(isNotifierEmail ? new WhatsAppNotificationSender() : new EmailNotificationSender());
        isNotifierEmail = !isNotifierEmail;
        updateNotificationButton();
        notifyUser(Alert.AlertType.INFORMATION, "Método de notificación actualizado", "Las alertas de inventario se enviarán por " + (isNotifierEmail ? "Email" : "WhatsApp") + ".");
    }

    @FXML private void openSqlConsole() {
        Dialog<ButtonType> dialog = centered(new Dialog<>()); dialog.setTitle("Consulta SQL personalizada"); dialog.setHeaderText("Ejecuta consultas de lectura o mantenimiento no destructivo");
        TextArea sql = new TextArea(); sql.setPromptText("SELECT TOP 20 * FROM Dim_Product"); sql.setPrefRowCount(8); sql.setPrefColumnCount(70);
        VBox content = new VBox(10, new Label("Consulta SQL"), sql); content.setPadding(new Insets(10)); dialog.getDialogPane().setContent(content);
        ButtonType run = new ButtonType("Ejecutar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(run, ButtonType.CANCEL);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == run) runSql(sql.getText());
    }

    @FXML private void resetDatabase() {
        TextInputDialog dialog = centered(new TextInputDialog()); dialog.setTitle("Vaciar base de datos"); dialog.setHeaderText("Esta acción eliminará todos los registros de todas las tablas. La estructura se conserva."); dialog.setContentText("Escribe CONFIRMAR para continuar:");
        dialog.showAndWait().ifPresent(value -> {
            if (!"CONFIRMAR".equals(value)) { notifyUser(Alert.AlertType.WARNING, "Operación cancelada", "La confirmación debe ser exactamente CONFIRMAR."); return; }
            Task<Void> task = new Task<>() { protected Void call() throws Exception { resetService.resetDatabaseConfirmed(); return null; } };
            task.setOnSucceeded(event -> { notifyUser(Alert.AlertType.INFORMATION, "Base de datos vaciada", "Todos los registros fueron eliminados correctamente."); showDashboard(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
        });
    }

    private void showTable(String table) {
        if (!TABLES.contains(table)) return;
        if (table.equals("Inventory")) {
            currentTable = table;
            pageTitle.setText(LABELS.get(table));
            tableCaption.setText("Inventario | usa Filtrar para consultar por nombre de producto");
            setTableActions(table);
            Task<TableData> task = new Task<>() { protected TableData call() throws Exception { return readTable(queryService(table, null)); } };
            task.setOnSucceeded(event -> { currentData = task.getValue(); dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); setConnected(); });
            task.setOnFailed(event -> showError(task.getException()));
            start(task);
            return;
        }
        currentTable = table; pageTitle.setText(LABELS.get(table)); tableCaption.setText(LABELS.get(table) + " | selecciona una fila para administrar"); setTableActions(table);
        Task<TableData> task = new Task<>() { protected TableData call() throws Exception { return readTable(queryService(table, null)); } };
        task.setOnSucceeded(event -> { currentData = task.getValue(); dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); setConnected(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void refreshDashboard() {
        Task<long[]> task = new Task<>() { protected long[] call() throws Exception { return new long[]{count("Dim_Product"), count("Dim_Customer"), count("Fact_Sales"), count("Inventory")}; } };
        task.setOnSucceeded(event -> { long[] v = task.getValue(); productsCount.setText("" + v[0]); customersCount.setText("" + v[1]); salesCount.setText("" + v[2]); stockCount.setText("" + v[3]); setConnected(); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void loadQuery(String table, String value) {
        Task<TableData> task = new Task<>() { protected TableData call() throws Exception { return readTable(queryService(table, value)); } };
        task.setOnSucceeded(event -> { currentData = task.getValue(); dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); tableCaption.setText(LABELS.get(table) + " | filtro aplicado"); setConnected(); });
        task.setOnFailed(event -> showError(task.getException()));
        start(task);
    }

    private QueryResult queryService(String table, String value) throws Exception {
        return switch (table) {
            case "Dim_Product" -> value == null ? productService.queryAllProducts() : productService.queryProductsByName(value);
            case "Dim_Customer" -> value == null ? customerService.queryAllCustomers() : customerService.queryCustomerByEmail(value);
            case "Dim_Supplier" -> value == null ? supplierService.queryAllSuppliers() : supplierService.querySupplierByEmail(value);
            case "Dim_ExpenseCategory" -> value == null ? categoryService.queryAllCategories() : categoryService.queryCategoryByName(value);
            case "Inventory" -> inventoryService.queryProductStock(value);
            case "Fact_Sales" -> value == null ? salesService.queryAllSales() : salesService.querySalesByProduct(value);
            case "Fact_MaterialExpenses" -> value == null ? expenseService.queryAllExpenses() : expenseService.queryExpensesByProduct(value);
            default -> throw new IllegalArgumentException("Sección no reconocida: " + table);
        };
    }

    private void editRecord(ObservableList<String> selected) {
        List<String> labels = switch (currentTable) {
            case "Dim_Product" -> List.of("Nombre", "Categoría", "Unidad", "Precio");
            case "Dim_Customer" -> List.of("Nombre", "Correo", "Teléfono");
            case "Dim_Supplier" -> List.of("Nombre", "Contacto", "Teléfono", "Correo");
            case "Dim_ExpenseCategory" -> selected == null ? List.of("Nombre") : List.of("Nombre anterior", "Nombre nuevo");
            case "Inventory" -> List.of("Producto", "Stock mínimo", "Cantidad disponible");
            default -> List.of();
        };
        GridPane grid = formGrid(labels);
        List<TextField> fields = fieldsOf(grid);
        fillServiceFields(selected, fields);
        if (currentTable.equals("Inventory") && selected != null) fields.get(0).setEditable(false);
        if (showForm(selected == null ? "Nuevo registro" : "Editar registro", grid)) invokeServiceEdit(selected, fields);
    }

    private void fillServiceFields(ObservableList<String> selected, List<TextField> fields) {
        if (selected == null || currentData == null) return;
        if (currentTable.equals("Dim_Product")) { fields.get(0).setText(value(selected, "product_name")); fields.get(1).setText(value(selected, "category")); fields.get(2).setText(value(selected, "unit_of_measure")); fields.get(3).setText(value(selected, "unit_price")); }
        if (currentTable.equals("Dim_Customer")) { fields.get(0).setText(value(selected, "customer_name")); fields.get(1).setText(value(selected, "email")); fields.get(2).setText(value(selected, "phone")); }
        if (currentTable.equals("Dim_Supplier")) { fields.get(0).setText(value(selected, "supplier_name")); fields.get(1).setText(value(selected, "contact_name")); fields.get(2).setText(value(selected, "phone")); fields.get(3).setText(value(selected, "email")); }
        if (currentTable.equals("Dim_ExpenseCategory")) fields.get(0).setText(value(selected, "category_name"));
        if (currentTable.equals("Inventory")) { fields.get(0).setText(value(selected, "product_name")); fields.get(1).setText(value(selected, "minimum_stock")); fields.get(2).setText(value(selected, "quantity_on_hand")); }
    }

    private String value(ObservableList<String> row, String column) { int index = currentData.columnNames.indexOf(column); if (index < 0) throw new IllegalStateException("La vista actual no incluye la columna " + column + "."); String cell = row.get(index); return cell == null ? "" : cell; }
    private static String optional(String text) { return text == null || text.isBlank() ? null : text; }
    private void invokeServiceEdit(ObservableList<String> selected, List<TextField> fields) {
        List<String> v = fields.stream().map(field -> field.getText().trim()).toList();
        ServiceAction action;
        try {
            action = switch (currentTable) {
                case "Dim_Product" -> { double price = Double.parseDouble(v.get(3)); if (selected == null) yield () -> productService.insertProductAndCreateInventory(v.get(0), v.get(1), v.get(2), price); int id = Integer.parseInt(value(selected, "product_id")); yield () -> productService.updateProduct(id, v.get(0), v.get(1), v.get(2), price); }
                case "Dim_Customer" -> { if (selected == null) yield () -> customerService.insertCustomer(v.get(0), optional(v.get(1)), optional(v.get(2))); int id = Integer.parseInt(value(selected, "customer_id")); yield () -> customerService.updateCustomer(id, v.get(0), optional(v.get(1)), optional(v.get(2))); }
                case "Dim_Supplier" -> { if (selected == null) yield () -> supplierService.insertSupplier(v.get(0), optional(v.get(1)), optional(v.get(2)), optional(v.get(3))); int id = Integer.parseInt(value(selected, "supplier_id")); yield () -> supplierService.updateSupplier(id, v.get(0), optional(v.get(1)), optional(v.get(2)), optional(v.get(3))); }
                case "Dim_ExpenseCategory" -> selected == null ? () -> categoryService.insertExpenseCategory(v.get(0)) : () -> categoryService.updateExpenseCategory(v.get(0), v.get(1));
                case "Inventory" -> { int minimum = Integer.parseInt(v.get(1)); int quantity = Integer.parseInt(v.get(2)); yield () -> { inventoryService.updateMinimumStock(v.get(0), minimum); inventoryService.updateQuantityOnHand(v.get(0), quantity); }; }
                default -> null;
            };
        } catch (NumberFormatException error) { notifyUser(Alert.AlertType.ERROR, "Datos inválidos", "Revisa los campos numéricos: " + error.getMessage()); return; }
        if (action != null) runService(action, currentTable);
    }

    private void deleteThroughService(ObservableList<String> selected) {
        ServiceAction action = switch (currentTable) {
            case "Dim_Product" -> { int id = Integer.parseInt(value(selected, "product_id")); yield () -> productService.deleteProduct(id); }
            case "Dim_Customer" -> { int id = Integer.parseInt(value(selected, "customer_id")); yield () -> customerService.deleteCustomer(id); }
            case "Dim_Supplier" -> { int id = Integer.parseInt(value(selected, "supplier_id")); yield () -> supplierService.deleteSupplier(id); }
            case "Dim_ExpenseCategory" -> { String name = value(selected, "category_name"); yield () -> categoryService.deleteExpenseCategory(name); }
            case "Fact_Sales" -> { int id = Integer.parseInt(value(selected, "sale_id")); yield () -> salesService.deleteSale(id); }
            case "Fact_MaterialExpenses" -> { int id = Integer.parseInt(value(selected, "expense_id")); yield () -> expenseService.deleteExpense(id); }
            default -> null;
        };
        if (action == null) { notifyUser(Alert.AlertType.WARNING, "Operación no disponible", "La consola no implementa eliminar registros de inventario."); return; }
        runService(action, currentTable);
    }

    private void runService(ServiceAction action, String refreshTable) {
        Task<Void> task = new Task<>() { protected Void call() throws Exception { action.run(); return null; } };
        task.setOnSucceeded(event -> { notifyUser(Alert.AlertType.INFORMATION, "Operación completada", "Los cambios se guardaron en la base de datos."); if (refreshTable != null) showTable(refreshTable); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void runSaleDialog(ObservableList<String> selected) {
        GridPane grid = formGrid(List.of("Producto", "Cantidad", "Método de pago", "Correo cliente (opcional)")); List<TextField> f = fieldsOf(grid);
        if (selected != null) { f.get(0).setText(value(selected, "product_name")); f.get(1).setText(value(selected, "quantity")); f.get(2).setText(value(selected, "payment_method")); f.get(3).setText(value(selected, "customer_email")); }
        if (!showForm(selected == null ? "Registrar venta" : "Editar venta", grid)) return;
        List<String> v = f.stream().map(field -> field.getText().trim()).toList();
        if (v.get(0).isEmpty() || v.get(2).isEmpty()) { notifyUser(Alert.AlertType.ERROR, "Datos incompletos", "Producto y método de pago son obligatorios."); return; }
        int quantity;
        try { quantity = Integer.parseInt(v.get(1)); } catch (NumberFormatException error) { notifyUser(Alert.AlertType.ERROR, "Datos inválidos", "La cantidad debe ser un número entero."); return; }
        if (selected == null) { runService(() -> salesService.insertSale(v.get(0), quantity, v.get(2), optional(v.get(3))), "Fact_Sales"); return; }
        int id = Integer.parseInt(value(selected, "sale_id"));
        runService(() -> salesService.updateSale(id, v.get(0), quantity, v.get(2), optional(v.get(3))), "Fact_Sales");
    }

    private void runExpenseDialog(ObservableList<String> selected) {
        GridPane grid = formGrid(List.of("Categoría", "Producto (opcional)", "Correo proveedor (opcional)", "Descripción", "Monto", "Método de pago", "Cantidad (opcional)")); List<TextField> f = fieldsOf(grid);
        if (selected != null) { List<String> columns = List.of("category_name", "product_name", "supplier_email", "description", "amount", "payment_method", "quantity"); for (int i = 0; i < columns.size(); i++) f.get(i).setText(value(selected, columns.get(i))); }
        if (!showForm(selected == null ? "Registrar gasto" : "Editar gasto", grid)) return;
        List<String> v = f.stream().map(field -> field.getText().trim()).toList();
        if (v.get(0).isEmpty() || v.get(3).isEmpty() || v.get(5).isEmpty()) { notifyUser(Alert.AlertType.ERROR, "Datos incompletos", "Categoría, descripción y método de pago son obligatorios."); return; }
        double amount; Integer quantity;
        try { amount = Double.parseDouble(v.get(4)); quantity = v.get(6).isEmpty() ? null : Integer.valueOf(v.get(6)); } catch (NumberFormatException error) { notifyUser(Alert.AlertType.ERROR, "Datos inválidos", "El monto debe ser numérico y la cantidad un número entero."); return; }
        if (selected == null) { runService(() -> expenseService.insertExpense(v.get(0), optional(v.get(1)), optional(v.get(2)), v.get(3), amount, v.get(5), quantity), "Fact_MaterialExpenses"); return; }
        int id = Integer.parseInt(value(selected, "expense_id"));
        runService(() -> expenseService.updateExpense(id, v.get(0), optional(v.get(1)), optional(v.get(2)), v.get(3), amount, v.get(5), quantity), "Fact_MaterialExpenses");
    }

    private void runSql(String sql) {
        String trimmed = sql == null ? "" : sql.trim(); if (trimmed.isEmpty()) return;
        if (trimmed.matches("(?is)^(DROP|TRUNCATE|DELETE)\\b.*")) { notifyUser(Alert.AlertType.WARNING, "Consulta bloqueada", "DROP, TRUNCATE y DELETE requieren usar la opción de limpieza con confirmación."); return; }
        if (trimmed.matches("(?is)^SELECT\\b.*")) runQuery(trimmed); else runService(() -> sqlService.execute(trimmed), null);
    }

    private void runQuery(String sql) {
        Task<TableData> task = new Task<>() { protected TableData call() throws Exception { return readTable(sqlService.query(sql)); } };
        task.setOnSucceeded(event -> { currentData = task.getValue(); currentTable = null; dataTable.getColumns().setAll(currentData.columns); dataTable.setItems(currentData.rows); pageTitle.setText("Consulta SQL"); tableCaption.setText("Resultado de consulta personalizada"); setConnected(); if (AppSettings.isExportSelectsToExcel()) runExport(sql); }); task.setOnFailed(event -> showError(task.getException())); start(task);
    }

    private void runExport(String sql) {
        Task<Void> task = new Task<>() { protected Void call() throws Exception { sqlService.exportQuery(sql, "resultado_" + System.currentTimeMillis() + ".xlsx"); return null; } };
        task.setOnSucceeded(event -> notifyUser(Alert.AlertType.INFORMATION, "Exportación completada", "Archivo generado en la carpeta exports.")); task.setOnFailed(event -> showError(task.getException())); start(task, true);
    }

    private long count(String table) throws Exception { QueryResult result = sqlService.query("SELECT COUNT(*) AS total FROM " + table); return Long.parseLong(result.rows().get(0).get(0)); }
    private static TableData readTable(QueryResult result) { List<String> names = result.columns(); ObservableList<TableColumn<ObservableList<String>, String>> columns = FXCollections.observableArrayList(); for (int i = 0; i < names.size(); i++) { int index = i; TableColumn<ObservableList<String>, String> column = new TableColumn<>(names.get(i)); column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(index))); column.setMinWidth(90); column.setPrefWidth(150); columns.add(column); } ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList(); for (List<String> sourceRow : result.rows()) rows.add(FXCollections.observableArrayList(sourceRow)); return new TableData(names, columns, rows); }
    private GridPane formGrid(List<String> labels) { GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10)); for (int i = 0; i < labels.size(); i++) { grid.add(new Label(labels.get(i)), 0, i); grid.add(new TextField(), 1, i); } return grid; }
    private List<TextField> fieldsOf(GridPane grid) { return grid.getChildren().stream().filter(node -> node instanceof TextField).map(node -> (TextField) node).toList(); }
    private boolean showForm(String title, GridPane grid) { Dialog<ButtonType> dialog = centered(new Dialog<>()); dialog.setTitle(title); dialog.getDialogPane().setContent(grid); ButtonType save = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL); return dialog.showAndWait().orElse(ButtonType.CANCEL) == save; }
    private void setTableActions(String table) {
        boolean canCreate = List.of("Dim_Product", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Fact_Sales", "Fact_MaterialExpenses").contains(table);
        boolean canEdit = List.of("Dim_Product", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Inventory", "Fact_Sales", "Fact_MaterialExpenses").contains(table);
        boolean canDelete = List.of("Dim_Product", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Fact_Sales", "Fact_MaterialExpenses").contains(table);
        setActionVisibility(createButton, canCreate);
        setActionVisibility(editButton, canEdit);
        setActionVisibility(deleteButton, canDelete);
        setActionVisibility(queryButton, true);
        queryButton.setText(table.equals("Dim_Supplier") ? "Filtrar proveedor" : "Filtrar");
    }

    private void setTableActions(boolean enabled) {
        setActionVisibility(createButton, enabled);
        setActionVisibility(editButton, enabled);
        setActionVisibility(deleteButton, enabled);
        setActionVisibility(queryButton, false);
    }

    private void setActionVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }
    private void updateExportStatus() { exportStatus.setText(AppSettings.isExportSelectsToExcel() ? "Exportación automática: activa" : "Exportación automática: desactivada"); }
    private void updateNotificationButton() { notificationButton.setText("Cambiar a " + (isNotifierEmail ? "WhatsApp" : "Email")); }
    private void setConnected() { connectionLabel.setText("Base de datos conectada"); connectionLabel.getStyleClass().setAll("connection-ok"); updateExportStatus(); }
    private void start(Task<?> task) { start(task, true); }
    private void start(Task<?> task, boolean showLoading) {
        activeTask = task;
        setLoading(showLoading);
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> finishLoading(task));
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> finishLoading(task));
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event -> finishLoading(task));
        Thread thread = new Thread(task, "business-sales-ui-task");
        thread.setDaemon(true);
        thread.start();
    }
    private void finishLoading(Task<?> task) { if (task == activeTask) setLoading(false); }
    private void setLoading(boolean loading) {
        tableContent.setDisable(loading);
        loadingOverlay.setVisible(loading);
        loadingOverlay.setManaged(loading);
        loadingIndicator.setVisible(loading);
        loadingLabel.setVisible(loading);
        loadingLabel.setManaged(loading);
        tableContent.getStyleClass().setAll("table-content");
        if (loading) tableContent.getStyleClass().add("table-content-loading");
    }
    private boolean confirm(String title, String message) { Alert alert = centered(new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK)); alert.setTitle(title); return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK; }
    private void showError(Throwable error) { notifyUser(Alert.AlertType.ERROR, "No se pudo completar la operación", error == null ? "Error desconocido" : error.getMessage()); }
    private void notifyUser(Alert.AlertType type, String title, String message) { Alert alert = centered(new Alert(type)); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.show(); }
    // Owned by the main window and centered on it, with the cursor already in the first field.
    private <T extends Dialog<?>> T centered(T dialog) {
        Window owner = dataTable.getScene().getWindow();
        dialog.initOwner(owner);
        dialog.setOnShown(event -> {
            Window window = dialog.getDialogPane().getScene().getWindow();
            window.setX(owner.getX() + (owner.getWidth() - window.getWidth()) / 2);
            window.setY(owner.getY() + (owner.getHeight() - window.getHeight()) / 2);
            Node firstInput = dialog.getDialogPane().lookup(".text-input");
            if (firstInput != null) Platform.runLater(firstInput::requestFocus);
        });
        return dialog;
    }
    @FunctionalInterface private interface ServiceAction { void run() throws Exception; }
    private record TableData(List<String> columnNames, ObservableList<TableColumn<ObservableList<String>, String>> columns, ObservableList<ObservableList<String>> rows) { }
}
