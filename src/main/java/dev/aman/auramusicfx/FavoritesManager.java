package dev.aman.auramusicfx;

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {

    private static final String FILE_NAME =
            "favorites.txt";

    public static Set<String> loadFavorites() {

        try {

            Path path =
                    AppPaths.getAppFolder()
                            .resolve(FILE_NAME);

            if (!Files.exists(path)) {
                return new HashSet<>();
            }

            return new HashSet<>(
                    Files.readAllLines(path)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
            return new HashSet<>();
        }
    }

    public static void saveFavorites(
            Set<String> favorites
    ) {

        try {

            Files.write(
                    AppPaths.getAppFolder()
                            .resolve(FILE_NAME),
                    favorites
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }
}