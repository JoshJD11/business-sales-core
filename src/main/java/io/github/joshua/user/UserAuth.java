package io.github.joshua.user;
import io.github.joshua.util.AppConfig;

public class UserAuth {

    private String username;
    private String password;

    public UserAuth() {
        this.username = AppConfig.get("APP_USER");
        this.password = AppConfig.get("APP_PASSWORD");
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
