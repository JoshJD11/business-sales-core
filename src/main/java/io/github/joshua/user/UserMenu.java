package io.github.joshua.user;
// import io.github.joshua.database.DBConnection;
// import io.github.joshua.production.ProductionService;
// import io.github.joshua.costs.BusinessExpense;
import java.util.Scanner;


public class UserMenu {

    private static final int MAX_ATTEMPTS = 3;

    private UserAuth userAuth;
    private Scanner scanner;

    public UserMenu() {
        this.userAuth = new UserAuth();
        this.scanner = new Scanner(System.in);
    }

    private boolean authenticateUser() {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (userAuth.login(username, password)) {
                return true;
            } else {
                attempts++;
                System.out.println("Invalid credentials. Attempts remaining: " + (MAX_ATTEMPTS - attempts));
            }
        }
        return false;
    }

    public void initialize() {
        System.out.println("Welcome to the Business Sales Core Application!");

        if (!authenticateUser()) {
            System.out.println("Maximum login attempts exceeded. Exiting application.");
            return;
        }



    }
}