package dev.aman.auramusicfx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaybackPositionManager {

    private static final String FILE_NAME = "position.txt";

    private static final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "PositionWriter-Thread");
        thread.setDaemon(true);
        return thread;
    });

    private static double lastSavedPosition = -1;

    public static void savePosition(double seconds) {
        if (Math.abs(seconds - lastSavedPosition) < 1.0) {
            return;
        }
        lastSavedPosition = seconds;

        writerExecutor.submit(() -> {
            try {
                Files.writeString(
                        AppPaths.getAppFolder().resolve(FILE_NAME),
                        String.valueOf(seconds)
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static double loadPosition() {
        try {
            Path path = AppPaths.getAppFolder().resolve(FILE_NAME);
            if (!Files.exists(path)) {
                return 0;
            }
            String content = Files.readString(path);
            lastSavedPosition = Double.parseDouble(content);
            return lastSavedPosition;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}