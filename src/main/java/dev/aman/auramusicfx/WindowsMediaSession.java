package dev.aman.auramusicfx;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public final class WindowsMediaSession implements AutoCloseable {

    public enum PlaybackStatus {
        PLAYING,
        PAUSED,
        STOPPED
    }

    private final Runnable onPlay;
    private final Runnable onPause;
    private final Runnable onPrevious;
    private final Runnable onNext;
    private Process process;
    private BufferedWriter writer;
    private final Thread shutdownHook;

    public WindowsMediaSession(
            Runnable onPlay,
            Runnable onPause,
            Runnable onPrevious,
            Runnable onNext
    ) {
        this.onPlay = onPlay;
        this.onPause = onPause;
        this.onPrevious = onPrevious;
        this.onNext = onNext;

        if (System.getProperty("os.name", "").toLowerCase().startsWith("windows")) {
            start();
        }

        shutdownHook = new Thread(this::close, "WindowsMediaSession-Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void start() {
        try (InputStream script = WindowsMediaSession.class.getResourceAsStream(
                "/windows-media-session.ps1"
        )) {
            if (script == null) {
                return;
            }

            Path scriptPath = AppPaths.getAppFolder().resolve("windows-media-session.ps1");
            Files.copy(script, scriptPath, StandardCopyOption.REPLACE_EXISTING);

            process = new ProcessBuilder(
                    "powershell.exe",
                    "-NoLogo",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    scriptPath.toString()
            ).redirectErrorStream(true).start();

            writer = new BufferedWriter(new OutputStreamWriter(
                    process.getOutputStream(),
                    StandardCharsets.UTF_8
            ));

            Thread readerThread = new Thread(
                    () -> readCommands(process.getInputStream()),
                    "WindowsMediaSession-Reader"
            );
            readerThread.setDaemon(true);
            readerThread.start();
        } catch (Exception ex) {
            System.err.println("Windows media controls unavailable: " + ex.getMessage());
        }
    }

    private void readCommands(InputStream input) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                input,
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("BUTTON\t")) {
                    continue;
                }
                dispatch(line.substring("BUTTON\t".length()));
            }
        } catch (Exception ignored) {
            // The stream closes normally when the application exits.
        }
    }

    private void dispatch(String button) {
        Runnable action = switch (button) {
            case "PLAY" -> onPlay;
            case "PAUSE" -> onPause;
            case "PREVIOUS" -> onPrevious;
            case "NEXT" -> onNext;
            default -> null;
        };

        if (action != null) {
            Platform.runLater(action);
        }
    }

    public void updateMetadata(String title, String artist, String album) {
        send("META\t" + encode(title) + "\t" + encode(artist) + "\t" + encode(album));
    }

    public void setPlaybackStatus(PlaybackStatus status) {
        send("STATUS\t" + status.name());
    }

    private String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getEncoder().encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    private synchronized void send(String command) {
        if (writer == null || process == null || !process.isAlive()) {
            return;
        }
        try {
            writer.write(command);
            writer.newLine();
            writer.flush();
        } catch (Exception ignored) {
            // Windows integration is optional; playback must remain unaffected.
        }
    }

    @Override
    public synchronized void close() {
        send("QUIT");
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ignored) {
        }
        if (process != null && process.isAlive()) {
            process.destroy();
        }
        writer = null;
        process = null;
    }
}
