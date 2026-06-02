package dev.aman.auramusicfx;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class LastSongManager {

    private static final String FILE_NAME =
            "lastsong.txt";

    public static void saveSong(File song) {

        try {

            Files.writeString(
                    AppPaths.getAppFolder().resolve(FILE_NAME),
                    song.getAbsolutePath()
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public static File loadSong() {

        try {

            Path path =
                    AppPaths.getAppFolder().resolve(FILE_NAME);

            if (!Files.exists(path)) {

                return null;
            }

            File song =
                    new File(
                            Files.readString(path)
                    );

            if (song.exists()) {

                return song;
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return null;
    }
}