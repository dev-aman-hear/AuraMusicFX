package dev.aman.auramusicfx;

import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import java.io.File;

public class VlcMediaManager {


    private final AudioPlayerComponent player =
            new AudioPlayerComponent();
    public VlcMediaManager() {

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
    public void play(File file) {

        System.out.println("VLC Playing: " + file.getAbsolutePath());

        player.mediaPlayer().media().play(file.getAbsolutePath());

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

        return player.mediaPlayer().status().isPlaying();
    }

    public void setVolume(int volume) {

        player.mediaPlayer().audio().setVolume(volume);
    }
    private Runnable endOfMediaListener;

    public void setEndOfMediaListener(Runnable listener) {
        this.endOfMediaListener = listener;
    }
    public long getDuration() {
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
        return player.mediaPlayer().status().time();
    }
    public void seek(long millis) {

        player.mediaPlayer().controls().setTime(millis);
    }
    public void pause() {
        player.mediaPlayer().controls().pause();
    }
    public void resume() {
        player.mediaPlayer().controls().play();
    }

    public void stop() {
        player.mediaPlayer().controls().stop();
    }
}