package dev.aman.auramusicfx;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LastFolderManager {

    private static final String FILE_NAME = "lastfolder.txt";

    private static final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "LastFolderWriter-Thread");
        thread.setDaemon(true);
        return thread;
    });

    public static void saveFolder(File folder) {
        writerExecutor.submit(() -> {
            try {
                Files.writeString(
                        AppPaths.getAppFolder().resolve(FILE_NAME),
                        folder.getAbsolutePath()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static File loadFolder() {
        try {
            Path path = AppPaths.getAppFolder().resolve(FILE_NAME);
            if (!Files.exists(path)) {
                return null;
            }
            String folderPath = Files.readString(path);
            File folder = new File(folderPath);
            if (folder.exists()) {
                return folder;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}