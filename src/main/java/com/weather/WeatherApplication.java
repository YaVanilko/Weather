package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

/**
 * Entry point of the Spring Boot application.
 * @EnableScheduling enables support for @Scheduled tasks.
 */
@SpringBootApplication
@EnableScheduling
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
        System.out.println("✅ Weather Service started! Open: http://localhost:8090");

        // Try to open the browser automatically
        openBrowserIfPossible();
    }

    private static void openBrowserIfPossible() {
        String url = "http://localhost:8090";

        // Method 1: Desktop API
        if (tryDesktopBrowser(url)) {
            return;
        }

        // Method 2: system commands
        if (trySystemCommand(url)) {
            return;
        }

        // Fallback: print the link and ask to open manually
        System.out.println("📌 Could not open the browser automatically.");
        System.out.println("🌐 Please open it manually: " + url);
    }

    private static boolean tryDesktopBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    URI uri = new URI(url);
                    desktop.browse(uri);
                    System.out.println("🌐 Browser opened automatically (Desktop API)!");
                    return true;
                }
            }
        } catch (Exception e) {
            // Desktop API failed, try system commands next
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
                // Linux: try common browser launchers in order
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
                System.out.println("🌐 Browser opened automatically (system command)!");
                return true;
            }
        } catch (IOException e) {
            // System command also failed
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
