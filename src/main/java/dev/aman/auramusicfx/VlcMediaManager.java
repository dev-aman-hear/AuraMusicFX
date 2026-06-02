package dev.aman.auramusicfx;

import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import java.io.File;

public class VlcMediaManager {

    private final AudioPlayerComponent player =
            new AudioPlayerComponent();

    public void play(File file) {

        player.mediaPlayer()
                .media()
                .play(file.getAbsolutePath());
    }
    public boolean isPlaying() {

        return player.mediaPlayer()
                .status()
                .isPlaying();
    }
    public long getDuration() {

        return player.mediaPlayer()
                .media()
                .info()
                .duration();
    }
    public long getCurrentTime() {

        return player.mediaPlayer()
                .status()
                .time();
    }
    public void seek(long millis) {

        player.mediaPlayer()
                .controls()
                .setTime(millis);
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