package dev.aman.auramusicfx;

import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;
import java.io.File;

public class VlcMediaManager {

    public void shutdown() {

        if (player == null) {
            return;
        }

        player.mediaPlayer().controls().stop();
        player.release();
    }

    private AudioPlayerComponent player;

    private void ensurePlayer() {

        if (player == null) {
            File vlcFolder =
                    new File(
                            System.getProperty("user.dir"),
                            "vlc"
                    );

            System.setProperty("jna.library.path", vlcFolder.getAbsolutePath());
            System.out.println("Bundled VLC = " + vlcFolder.getAbsolutePath());

            player = new AudioPlayerComponent();
            player.mediaPlayer()
                    .events()
                    .addMediaPlayerEventListener(
                            new MediaPlayerEventAdapter() {

                                @Override
                                public void finished(
                                        uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer
                                ) {

                                    if (endOfMediaListener != null) {

                                        javafx.application.Platform.runLater(
                                                endOfMediaListener
                                        );
                                    }
                                }
                            }
                    );
        }
    }

    public void play(File file) {

        try {

            ensurePlayer();

        }catch (Throwable ex) {

            System.out.println("VLC ERROR:");
            ex.printStackTrace();

            javafx.application.Platform.runLater(() -> {

                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR,
                        ex.toString()
                ).showAndWait();
            });

            return;
        }
        System.out.println("VLC Playing: " + file.getAbsolutePath());

        boolean started =
                player.mediaPlayer()
                        .media()
                        .play(file.getAbsolutePath());

        System.out.println("Started = " + started);


        new Thread(() -> {

            try {

                Thread.sleep(3000);

                System.out.println("Duration = " + getDuration());

                System.out.println("Current = " + getCurrentTime());

            } catch (Exception ex) {

                ex.printStackTrace();
            }

        }).start();
    }
    public boolean isPlaying() {
        if (player == null) {
            return false;
        }
        return player.mediaPlayer().status().isPlaying();
    }

    public void setVolume(int volume) {
        if (player == null) {
            return;
        }
        player.mediaPlayer().audio().setVolume(volume);
    }
    private Runnable endOfMediaListener;

    public void setEndOfMediaListener(Runnable listener) {
        this.endOfMediaListener = listener;
    }
    public long getDuration() {
        if (player == null) {
            return 0;
        }
        try {
            if (
                    player.mediaPlayer().media().info() == null
            ) {
                return 0;
            }
            return player.mediaPlayer().media().info().duration();

        } catch (Exception ex) {
            return 0;
        }
    }

    public long getCurrentTime() {
        if (player == null) {
            return 0;
        }
        return player.mediaPlayer().status().time();
    }
    public void seek(long millis) {
        if (player == null) {
            return;
        }
        player.mediaPlayer().controls().setTime(millis);
    }
    public void pause() {
        if (player == null) {
            return;
        }
        player.mediaPlayer().controls().pause();
    }
    public void resume() {
        if (player == null) {
            return;
        }
        player.mediaPlayer().controls().play();
    }

    public void stop() {

        if (player == null) {
            return;
        }

        player.mediaPlayer()
                .controls()
                .stop();
    }
}