package io.github.joshua.user;
import io.github.joshua.inventory.InventoryManager;
import io.github.joshua.sales.SalesService;
import io.github.joshua.costs.BusinessExpense;
import java.util.Scanner;


public class UserMenu {

    private static final int MAX_ATTEMPTS = 3;

    private UserAuth userAuth;
    private Scanner scanner;
    private InventoryManager inventoryManager;
    private SalesService salesService;
    private BusinessExpense businessExpense;

    public UserMenu() {
        this.userAuth = new UserAuth();
        this.scanner = new Scanner(System.in);
        this.inventoryManager = new InventoryManager("whatsApp"); // whatsApp or email
        this.salesService = new SalesService();
        this.businessExpense = new BusinessExpense();
    }

    private boolean authenticateUser() {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Ingrese su usuario: ");
            String username = scanner.nextLine();
            System.out.print("Ingrese su contraseña: ");
            String password = scanner.nextLine();

            if (userAuth.login(username, password)) {
                return true;
            } else {
                attempts++;
                System.out.println("Credenciales inválidas. Intentos restantes: " + (MAX_ATTEMPTS - attempts));
            }
        }
        return false;
    }

    public void initialize() {
        System.out.println("Bienvenido al sistema de ventas y gastos de la empresa!");

        if (!authenticateUser()) {
            System.out.println("Se excedió el número máximo de intentos de inicio de sesión. Saliendo de la aplicación.");
            return;
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Registrar venta");
            System.out.println("2. Consultar ventas por producto");
            System.out.println("3. Registrar gasto");
            System.out.println("4. Consultar gasto por producto");
            System.out.println("5. Consultar inventario del producto");
            System.out.println("6. Actualizar stock mínimo del producto");
            System.out.println("7. Actualizar cantidad de un producto en inventario");
            System.out.println("8. Salir");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre del producto: ");
                    String soldProductName = scanner.nextLine();
                    System.out.print("Ingrese la cantidad: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String customerPaymentMethod = scanner.nextLine();
                    System.out.print("Ingrese el correo del cliente, en caso de no tener uno, presione enter: ");
                    String customerEmail = scanner.nextLine();
                    salesService.insertSale(soldProductName, quantity, customerPaymentMethod, customerEmail);
                    break;
                case "2":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForSales = scanner.nextLine();
                    salesService.consultSalesByProduct(productNameForSales);
                    break;
                case "3":
                    System.out.print("Ingrese el nombre de la categoría: ");
                    String categoryName = scanner.nextLine();
                    System.out.print("Ingrese el nombre del producto: ");
                    String productName = scanner.nextLine();
                    System.out.print("Ingrese el correo del proveedor: ");
                    String supplierEmail = scanner.nextLine();
                    System.out.print("Ingrese la descripción del gasto: ");
                    String description = scanner.nextLine();
                    System.out.print("Ingrese el monto del gasto: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String paymentMethod = scanner.nextLine();
                    System.out.print("Ingrese la cantidad: ");
                    int productQuantity = Integer.parseInt(scanner.nextLine());
                    businessExpense.insertExpense(categoryName, productName, supplierEmail, description, amount, paymentMethod, productQuantity);
                    break;
                case "4":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForExpense = scanner.nextLine();
                    businessExpense.consultExpenseByProduct(productNameForExpense);
                    break;
                case "5":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForInventory = scanner.nextLine();
                    inventoryManager.consultProductStock(productNameForInventory);
                    break;
                case "6":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForMinStock = scanner.nextLine();
                    System.out.print("Ingrese el nuevo límite del stock: ");
                    try {
                        int newMinStock = Integer.parseInt(scanner.nextLine());
                        inventoryManager.updateMinimumStock(productNameForMinStock, newMinStock);
                    } catch (NumberFormatException e) {
                        System.out.println("Límite de stock inválido. Por favor, ingrese un número entero.");
                    }

                    break;
                case "7":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameToUpdate = scanner.nextLine();
                    System.out.print("Ingrese la nueva cantidad: ");
                    try {
                        int newQuantity = Integer.parseInt(scanner.nextLine());
                        inventoryManager.updateQuantityOnHand(productNameToUpdate, newQuantity);
                    } catch (NumberFormatException e) {
                        System.out.println("Cantidad inválida. Por favor, ingrese un número entero.");
                    }

                    break;
                case "8":
                    exit = true;
                    System.out.println("Saliendo de la aplicación.");
                    break;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
            }
        }
    }
}