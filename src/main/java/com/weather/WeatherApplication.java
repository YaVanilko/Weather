package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

/**
 * Точка входу у Spring Boot додаток.
 * @EnableScheduling — вмикає підтримку @Scheduled (планувальника завдань)
 */
@SpringBootApplication
@EnableScheduling
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
        System.out.println("✅ Weather Service запущено! Відкрий: http://localhost:8090");

        // Спробуємо відкрити браузер автоматично
        openBrowserIfPossible();
    }

    private static void openBrowserIfPossible() {
        String url = "http://localhost:8090";

        // Спосіб 1: Спробуємо Desktop API
        if (tryDesktopBrowser(url)) {
            return;
        }

        // Спосіб 2: Спробуємо системні команди
        if (trySystemCommand(url)) {
            return;
        }

        // Fallback: просто виведемо посилання
        System.out.println("📌 Браузер не вдалось відкрити автоматично.");
        System.out.println("🌐 Будь ласка, відкрийте вручну: " + url);
    }

    private static boolean tryDesktopBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    URI uri = new URI(url);
                    desktop.browse(uri);
                    System.out.println("🌐 Браузер відкритий автоматично (Desktop API)!");
                    return true;
                }
            }
        } catch (Exception e) {
            // Desktop API не спрацював — спробуємо системні команди
        }
        return false;
    }

    private static boolean trySystemCommand(String url) {
        String osName = System.getProperty("os.name").toLowerCase();
        String[] command = null;

        try {
            if (osName.contains("win")) {
                // Windows
                command = new String[]{"cmd", "/c", "start", url};
            } else if (osName.contains("mac")) {
                // macOS
                command = new String[]{"open", url};
            } else if (osName.contains("nix") || osName.contains("nux")) {
                // Linux — спробуємо різні браузери по порядку
                if (isCommandAvailable("xdg-open")) {
                    command = new String[]{"xdg-open", url};
                } else if (isCommandAvailable("firefox")) {
                    command = new String[]{"firefox", url};
                } else if (isCommandAvailable("chromium-browser")) {
                    command = new String[]{"chromium-browser", url};
                } else if (isCommandAvailable("google-chrome")) {
                    command = new String[]{"google-chrome", url};
                }
            }

            if (command != null) {
                Runtime.getRuntime().exec(command);
                System.out.println("🌐 Браузер відкритий автоматично (системна команда)!");
                return true;
            }
        } catch (IOException e) {
            // Системна команда також не спрацювала
        }
        return false;
    }

    private static boolean isCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", command});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
