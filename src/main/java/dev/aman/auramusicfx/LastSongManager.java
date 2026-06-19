package dev.aman.auramusicfx;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LastSongManager {

    private static final String FILE_NAME = "lastsong.txt";

    private static final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "LastSongWriter-Thread");
        thread.setDaemon(true);
        return thread;
    });

    public static void saveSong(File song) {
        writerExecutor.submit(() -> {
            try {
                Files.writeString(
                        AppPaths.getAppFolder().resolve(FILE_NAME),
                        song.getAbsolutePath()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static File loadSong() {
        try {
            Path path = AppPaths.getAppFolder().resolve(FILE_NAME);
            if (!Files.exists(path)) {
                return null;
            }
            File song = new File(Files.readString(path));
            if (song.exists()) {
                return song;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}