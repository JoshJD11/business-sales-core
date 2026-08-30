package io.github.joshua.user;
import io.github.cdimascio.dotenv.Dotenv;

public class UserAuth {

    private String username;
    private String password;

    public UserAuth() {
        Dotenv dotenv = Dotenv.load();
        this.username = dotenv.get("APP_USER");
        this.password = dotenv.get("APP_PASSWORD");
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
