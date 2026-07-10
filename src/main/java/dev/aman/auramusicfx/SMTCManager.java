package dev.aman.auramusicfx;

import io.github.selemba1000.*;
import javafx.application.Platform;

import java.io.File;

public class SMTCManager {

    private final MediaManager mediaManager;
    private final ControlsView controlsView;
    private JMTC jmtc;

    public SMTCManager(MediaManager mediaManager, ControlsView controlsView) {
        this.mediaManager = mediaManager;
        this.controlsView = controlsView;
    }

    public void init() {
        new Thread(() -> {
            try {
                // Do not initialize COM manually; winrt::init_apartment() handles it in the DLL
                jmtc = JMTC.getInstance(new JMTCSettings("AuraMusicFX", "AuraMusicFX"));

                JMTCCallbacks callbacks = new JMTCCallbacks();
                callbacks.onPlay = () -> Platform.runLater(() -> controlsView.getPlayButton().fire());
                callbacks.onPause = () -> Platform.runLater(() -> controlsView.getPlayButton().fire());
                callbacks.onNext = () -> Platform.runLater(() -> controlsView.getNextButton().fire());
                callbacks.onPrevious = () -> Platform.runLater(() -> controlsView.getPreviousButton().fire());

                jmtc.setCallbacks(callbacks);
                jmtc.setEnabled(true);
                jmtc.setMediaType(JMTCMediaType.Music);
                jmtc.setPlayingState(JMTCPlayingState.PAUSED);

                jmtc.setEnabledButtons(new JMTCEnabledButtons(
                        true, // play
                        true, // pause
                        true, // stop
                        true, // next
                        true  // prev
                ));

                jmtc.updateDisplay();

                // Register song change listener to update SMTC metadata
                mediaManager.addSongChangeListener(new MediaManager.SongChangeListener() {
                    @Override
                    public void onSongChanged(File file) {
                        updateMetadata();
                    }
                });

                // Keep thread alive so the COM apartment doesn't die
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to initialize SMTC: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, "SMTC-Thread").start();
    }

    public void updateMetadata() {
        if (jmtc == null) return;

        String title = mediaManager.getCurrentTitle();
        String artist = mediaManager.getCurrentArtist();
        String album = mediaManager.getCurrentAlbum();

        if (title == null || title.isBlank()) title = "Unknown Title";
        if (artist == null || artist.isBlank()) artist = "Unknown Artist";
        if (album == null || album.isBlank()) album = "Unknown Album";

        File artworkFile = mediaManager.getCurrentArtworkFile();

        JMTCMusicProperties properties = new JMTCMusicProperties(
                title,
                artist,
                album,
                artist, // albumArtist
                new String[]{}, // genres
                0, // track
                1, // trackCount
                artworkFile // cover
        );

        jmtc.setMediaProperties(properties);
        jmtc.setPlayingState(JMTCPlayingState.PLAYING);
        jmtc.updateDisplay();
    }
}
