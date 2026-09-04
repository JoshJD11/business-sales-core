package io.github.joshua.user;
import io.github.joshua.inventory.InventoryManager;
import io.github.joshua.sales.SalesService;
import io.github.joshua.costs.BusinessExpense;
import java.util.Scanner;
import io.github.joshua.supplier.SupplierService;
import io.github.joshua.product.ProductService;
import io.github.joshua.admin.DatabaseResetService;
import io.github.joshua.admin.SqlConsoleService;
import io.github.joshua.expensecategory.ExpenseCategory;
import io.github.joshua.customer.CustomerService;


public class UserMenu {

    private static final int MAX_ATTEMPTS = 3;

    private UserAuth userAuth;
    private Scanner scanner;
    private InventoryManager inventoryManager;
    private SalesService salesService;
    private BusinessExpense businessExpense;
    private SupplierService supplierService;
    private ProductService productService;
    private DatabaseResetService databaseResetService;
    private SqlConsoleService sqlConsoleService;
    private CustomerService customerService;
    private ExpenseCategory expenseCategory;

    public UserMenu() {
        this.userAuth = new UserAuth();
        this.scanner = new Scanner(System.in);
        this.inventoryManager = new InventoryManager("whatsApp"); // whatsApp or email
        this.salesService = new SalesService();
        this.businessExpense = new BusinessExpense();
        this.supplierService = new SupplierService();
        this.productService = new ProductService();
        this.databaseResetService = new DatabaseResetService();
        this.sqlConsoleService = new SqlConsoleService();
        this.expenseCategory = new ExpenseCategory();
        this.customerService = new CustomerService();
    }

    private boolean authenticateUser() { // Security can be improved by implementing password hashing, salting, and using a more secure authentication mechanism. This is a basic implementation.
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
            System.out.println("1. Registro de ventas");
            System.out.println("2. Registro de gastos");
            System.out.println("3. Inventario del negocio");
            System.out.println("4. Manejo de productos");
            System.out.println("5. Proovedores");
            System.out.println("6. Clientes");
            System.out.println("7. Categorías de gastos");
            System.out.println("8. Vaciar base de datos (¡Cuidado! Esto eliminará todos los registros)");
            System.out.println("9. Ejecutar consulta SQL personalizada (¡Cuidado! Esto puede afectar la base de datos)");
            System.out.println("10. Salir");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    salesService.init();
                    break;

                case "2":
                    businessExpense.init();
                    break;

                case "3":
                    inventoryManager.init();
                    break;

                case "4":
                    productService.init();
                    break;
                case "5":
                    supplierService.init();
                    break;
                    
                case "6":
                    customerService.init();
                    break;

                case "7":
                    expenseCategory.init();
                    break;
                
                case "8":
                    databaseResetService.resetDatabase();
                    break;
                case "9":
                    System.out.print("Ingrese la consulta SQL a ejecutar: ");
                    String sqlQuery = scanner.nextLine();
                    sqlConsoleService.executeCustomQuery(sqlQuery);
                    break;
                case "10":
                    exit = true;
                    System.out.println("Saliendo de la aplicación.");
                    break;
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
            }
        }
    }
}
