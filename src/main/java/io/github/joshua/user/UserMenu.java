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

                    break;
                case "2":

                    break;
                case "3":

                    break;
                case "4":

                    break;
                case "5":
                    
                    break;
                case "6":

                    break;
                case "7":
                    
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