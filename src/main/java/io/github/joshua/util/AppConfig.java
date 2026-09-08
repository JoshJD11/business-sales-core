package io.github.joshua.util;

import io.github.cdimascio.dotenv.Dotenv;

public final class AppConfig {

    private static final Dotenv DOTENV = Dotenv.load();

    private AppConfig() {
    }

    public static String get(String key) {
        return DOTENV.get(key);
    }
}
