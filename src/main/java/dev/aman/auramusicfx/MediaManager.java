package dev.aman.auramusicfx;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class MediaManager {

    public enum RepeatMode {

        OFF,

        ALL,

        ONE
    }

    private RepeatMode repeatMode =
            RepeatMode.ALL;
    public void cycleRepeatMode() {

        switch (repeatMode) {

            case OFF ->
                    repeatMode =
                            RepeatMode.ALL;

            case ALL ->
                    repeatMode =
                            RepeatMode.ONE;

            case ONE ->
                    repeatMode =
                            RepeatMode.OFF;
        }
    }

    public RepeatMode getRepeatMode() {

        return repeatMode;
    }

    private MediaPlayer mediaPlayer;
    private final VlcMediaManager vlcManager = new VlcMediaManager();

    private Image currentArtwork;

    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "Unknown Album";
    public String getCurrentArtist() {
        return currentArtist;
    }
    public String getCurrentAlbum() {
        return currentAlbum;
    }

    private List<File> songs = new ArrayList<>();

    private int currentSongIndex = -1;
    private boolean shuffleEnabled = false;
    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
    }


    private ChangeListener<Duration>
            lyricsListener;
    private SongChangeListener
            songChangeListener;
    public void setSongChangeListener(

            SongChangeListener listener
    ) {

        this.songChangeListener =
                listener;
    }

    public Image getCurrentArtwork() {

        return currentArtwork;
    }

    public void addSong(File file) {

        songs.add(file);
    }

    public List<File> getSongs() {
        return songs;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void clearSongs() {

        songs.clear();
    }
    public void setLyricsListener(

            ChangeListener<Duration> listener
    ) {

        if (
                mediaPlayer != null
                        && lyricsListener != null
        ) {

            mediaPlayer.currentTimeProperty()
                    .removeListener(
                            lyricsListener
                    );
        }

        lyricsListener = listener;

        if (mediaPlayer != null) {

            mediaPlayer.currentTimeProperty()
                    .addListener(
                            lyricsListener
                    );
        }
    }

    public void seek(double percent) {

        if (mediaPlayer == null) {
            return;
        }

        Duration total =
                mediaPlayer.getTotalDuration();

        mediaPlayer.seek(
                total.multiply(percent)
        );
    }

    public void playSong(File file) {
        currentArtwork = null;
        currentArtist = "Unknown Artist";
        currentAlbum = "Unknown Album";

        currentSongIndex = songs.indexOf(file);

        try {
            try {

                if (file.getName().toLowerCase().endsWith(".mp3")) {

                    Mp3File mp3 = new Mp3File(file);

                    if (mp3.hasId3v2Tag()) {

                        ID3v2 id3v2 = mp3.getId3v2Tag();

                        if (id3v2.getArtist() != null && !id3v2.getArtist().isBlank()) {
                            currentArtist = id3v2.getArtist();

                        } else {
                            currentArtist = "Unknown Artist";
                        }
                        if (id3v2.getAlbum() != null &&
                                !id3v2.getAlbum().isBlank()) {

                            currentAlbum =
                                    id3v2.getAlbum();

                        } else {

                            currentAlbum =
                                    "Unknown Album";
                        }
                        byte[] imageData = id3v2.getAlbumImage();

                        if (imageData != null) {

                            currentArtwork = new Image(

                                    new ByteArrayInputStream(imageData)
                            );
                        }
                    }
                }
            } catch (Exception ex) {

                System.out.println(
                        "Cannot play: "
                                + file.getName()
                );

                ex.printStackTrace();
            }

            Media media = new Media(file.toURI().toString());
            if (
                    file.getName().toLowerCase().endsWith(".flac")
                            ||
                            file.getName().toLowerCase().endsWith(".m4a")
            ) {

                vlcManager.play(file);

                if (songChangeListener != null) {
                    songChangeListener.onSongChanged(file);
                }

                return;
            }

            if (mediaPlayer != null) {

                mediaPlayer.stop();

                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnError(() -> {

                System.out.println(
                        "MediaPlayer Error: "
                                + mediaPlayer.getError()
                );

                Platform.runLater(() -> {

                    javafx.scene.control.Alert alert =
                            new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR
                            );

                    alert.setHeaderText(
                            "Unsupported Audio Format"
                    );

                    alert.setContentText(
                            file.getName()
                                    + "\n\nThis file cannot be played by JavaFX."
                    );

                    alert.showAndWait();
                });
            });
            mediaPlayer.setOnReady(() -> {

                File lastSong =
                        LastSongManager.loadSong();

                if (
                        lastSong != null &&
                                lastSong.equals(file)
                ) {

                    double savedPosition =
                            PlaybackPositionManager.loadPosition();

                    if (savedPosition > 0) {

                        mediaPlayer.seek(
                                Duration.seconds(savedPosition)
                        );
                    }
                }
            });
            mediaPlayer.play();
            LastSongManager.saveSong(file);
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                PlaybackPositionManager.savePosition(newTime.toSeconds());
            });

            if (songChangeListener != null) {

                songChangeListener.onSongChanged(file);
            }
            mediaPlayer.setOnEndOfMedia(() -> {

                switch (repeatMode) {

                    case ONE -> {

                        playSong(
                                songs.get(currentSongIndex)
                        );
                    }

                    case ALL -> {

                        File nextSong =
                                nextSong();

                        if (nextSong != null) {

                            playSong(nextSong);
                        }
                    }

                    case OFF -> {

                        if (currentSongIndex
                                < songs.size() - 1) {

                            File nextSong =
                                    nextSong();

                            playSong(nextSong);
                        }
                    }
                }
            });
        } catch (Exception ex) {

            Platform.runLater(() -> {

                javafx.scene.control.Alert alert =
                        new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR
                        );

                alert.setHeaderText(
                        "Unsupported Audio Format"
                );

                alert.setContentText(
                        file.getName()
                                + "\n\nThis file cannot be played by JavaFX."
                );

                alert.showAndWait();
            });

            return;
        }
    }

//interface

    public interface SongChangeListener {

        void onSongChanged(File song);
    }

    public File getCurrentSong() {

        if (songs.isEmpty())
            return null;

        return songs.get(currentSongIndex);
    }
    public File previousSong() {

        if (songs.isEmpty())
            return null;

        currentSongIndex--;

        if (currentSongIndex < 0) {

            currentArtwork = null;
            currentSongIndex = songs.size() - 1;
        }

        return songs.get(currentSongIndex);
    }

    public File nextSong() {

        if (songs.isEmpty())
            return null;

        if (shuffleEnabled) {

            int randomIndex;

            do {

                randomIndex =
                        (int)(Math.random() * songs.size());

            } while (
                    songs.size() > 1 &&
                            randomIndex == currentSongIndex
            );

            currentSongIndex =
                    randomIndex;

        } else {

            currentSongIndex++;

            if (currentSongIndex >= songs.size()) {

                currentSongIndex = 0;
            }
        }

        return songs.get(currentSongIndex);
    }

    public void pause() {

        if (mediaPlayer != null) {

            mediaPlayer.pause();
        }
    }

    public void resume() {

        if (mediaPlayer != null) {

            mediaPlayer.play();
        }
    }

    public void togglePlayPause() {

        if (mediaPlayer == null)
            return;

        if (mediaPlayer.getStatus()
                == javafx.scene.media.MediaPlayer.Status.PLAYING) {

            mediaPlayer.pause();

        } else {

            mediaPlayer.play();
        }
    }

    public boolean isPlaying() {

        if (mediaPlayer == null)
            return false;

        return mediaPlayer.getStatus()
                == javafx.scene.media.MediaPlayer.Status.PLAYING;
    }

}