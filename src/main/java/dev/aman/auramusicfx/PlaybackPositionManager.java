package dev.aman.auramusicfx;

import java.nio.file.Files;
import java.nio.file.Path;

public class PlaybackPositionManager {

    private static final String FILE_NAME =
            "position.txt";

    public static void savePosition(
            double seconds
    ) {

        try {

            Files.writeString(
                    AppPaths.getAppFolder().resolve(FILE_NAME),
                    String.valueOf(seconds)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public static double loadPosition() {

        try {

            Path path =
                    AppPaths.getAppFolder().resolve(FILE_NAME);

            if (!Files.exists(path)) {

                return 0;
            }

            return Double.parseDouble(
                    Files.readString(path)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return 0;
    }
}