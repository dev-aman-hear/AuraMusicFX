package dev.aman.auramusicfx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VolumeManager {

    private static final String FILE_NAME = "volume.txt";

    private static final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "VolumeWriter-Thread");
        thread.setDaemon(true);
        return thread;
    });

    private static double lastSavedVolume = -1;

    public static void saveVolume(double volume) {
        if (Math.abs(volume - lastSavedVolume) < 0.5) {
            return;
        }
        lastSavedVolume = volume;

        writerExecutor.submit(() -> {
            try {
                Files.writeString(
                        AppPaths.getAppFolder().resolve(FILE_NAME),
                        String.valueOf(volume)
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static double loadVolume() {
        try {
            Path path = AppPaths.getAppFolder().resolve(FILE_NAME);
            if (!Files.exists(path)) {
                return 100.0;
            }
            String content = Files.readString(path);
            lastSavedVolume = Double.parseDouble(content);
            return lastSavedVolume;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 100.0;
    }
}