package dev.aman.auramusicfx;

import java.nio.file.Files;
import java.nio.file.Path;

public class VolumeManager {

    private static final String FILE_NAME =
            "volume.txt";

    public static void saveVolume(double volume) {

        try {

            Files.writeString(
                    AppPaths.getAppFolder()
                            .resolve(FILE_NAME),
                    String.valueOf(volume)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public static double loadVolume() {

        try {

            Path path =
                    AppPaths.getAppFolder()
                            .resolve(FILE_NAME);

            if (!Files.exists(path)) {

                return 100.0;
            }

            return Double.parseDouble(
                    Files.readString(path)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return 100.0;
    }
}