package dev.aman.auramusicfx;

import java.nio.file.Files;
import java.nio.file.Path;

public class AppPaths {

    private static final Path APP_FOLDER =
            Path.of(
                    System.getenv("APPDATA"),
                    "AuraMusicFX"
            );

    static {
        try {
            Files.createDirectories(APP_FOLDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Path getAppFolder() {
        return APP_FOLDER;
    }
}