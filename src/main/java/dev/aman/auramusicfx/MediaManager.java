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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;

public class MediaManager {

    public static class SongMetadata {
        public final String title;
        public final String artist;

        public SongMetadata(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }
    }

    private final ConcurrentHashMap<File, SongMetadata> metadataCache = new ConcurrentHashMap<>();

    public SongMetadata getSongMetadata(File file) {
        return metadataCache.computeIfAbsent(file, f -> {
            String title = "Unknown Title";
            String artist = "Unknown Artist";
            try {
                AudioFile audioFile = AudioFileIO.read(f);
                Tag tag = audioFile.getTag();
                if (tag != null) {
                    String t = tag.getFirst(FieldKey.TITLE);
                    String a = tag.getFirst(FieldKey.ARTIST);
                    if (t != null && !t.isBlank()) {
                        title = t;
                    } else {
                        String name = f.getName();
                        int dot = name.lastIndexOf('.');
                        title = dot > 0 ? name.substring(0, dot) : name;
                    }
                    if (a != null && !a.isBlank()) {
                        artist = a;
                    }
                } else {
                    String name = f.getName();
                    int dot = name.lastIndexOf('.');
                    title = dot > 0 ? name.substring(0, dot) : name;
                }
            } catch (Exception ignored) {
                String name = f.getName();
                int dot = name.lastIndexOf('.');
                title = dot > 0 ? name.substring(0, dot) : name;
            }
            return new SongMetadata(title, artist);
        });
    }

    private String currentTitle = "Unknown Title";

    public String getCurrentTitle() {
        return currentTitle;
    }
    public void restorePosition() {

        File song =
                getCurrentSong();

        File lastSong =
                LastSongManager.loadSong();

        if (song == null
                || lastSong == null
                || !song.equals(lastSong)) {
            return;
        }

        double savedPosition = PlaybackPositionManager.loadPosition();

        new Thread(() -> {

            try {

                Thread.sleep(1500);

                vlcManager.seek((long)(savedPosition * 1000));

            } catch (Exception ex) {

                ex.printStackTrace();
            }

        }).start();
    }

    public MediaManager() {

        vlcManager.setEndOfMediaListener(() -> {
            switch (repeatMode) {
                case ONE -> {
                    playSong(songs.get(currentSongIndex));
                }
                case ALL -> {
                    File nextSong = nextSong();
                    if (nextSong != null) {
                        playSong(nextSong);
                    }
                }
                case OFF -> {
                    if (currentSongIndex < songs.size() - 1) {
                        File nextSong = nextSong();
                        playSong(nextSong);
                    }
                }
            }
        });
    }

    public String[] getAudioInfo(File file) {

        try {

            AudioFile audioFile =
                    AudioFileIO.read(file);

            var header =
                    audioFile.getAudioHeader();

            int sampleRate =
                    Integer.parseInt(
                            header.getSampleRate()
                    );

            String format;

            if (
                    (file.getName().toLowerCase().endsWith(".flac")
                            || file.getName().toLowerCase().endsWith(".m4a"))
                            && sampleRate > 48000
            ) {

                format = "Hi-Res Lossless";

            } else if (
                    file.getName().toLowerCase().endsWith(".flac")
                            || file.getName().toLowerCase().endsWith(".m4a")
            ) {

                format = "Lossless";

            } else if (
                    header.getFormat()
                            .toLowerCase()
                            .contains("mp3")
            ) {

                format = "MP3";

            } else {

                format = header.getFormat();
            }

            double khz =
                    sampleRate / 1000.0;

            String details;

            if (file.getName().toLowerCase().endsWith(".m4a")) {

                details =
                        String.format(
                                "%.0f kHz • ALAC",
                                khz
                        );

            } else if (file.getName().toLowerCase().endsWith(".flac")) {

                details =
                        String.format(
                                "%.1f kHz • FLAC",
                                khz
                        );

            } else {

                details =
                        header.getBitRate()
                                + " kbps";
            }

            return new String[] {
                    format,
                    details
            };

        } catch (Exception ex) {

            ex.printStackTrace();

            return new String[] {
                    "Audio",
                    ""
            };
        }
    }

    public enum RepeatMode {

        OFF,

        ALL,

        ONE
    }

    public boolean isVlcSong() {
        File song = getCurrentSong();

        if (song == null) {
            return false;
        }

        String name =
                song.getName().toLowerCase();

        return name.endsWith(".m4a")
                || name.endsWith(".flac");
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
    public VlcMediaManager getVlcManager() {
        return vlcManager;
    }
    private Image currentArtwork;

    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "Unknown Album";
    public String getCurrentArtist() {
        return currentArtist;
    }
    public String getCurrentAlbum() {
        return currentAlbum;
    }

    private void loadMetadata(File file) {

        try {

            AudioFile audioFile =
                    AudioFileIO.read(file);

            Tag tag =
                    audioFile.getTag();

            if (tag == null) {
                return;
            }

            String title =
                    tag.getFirst(FieldKey.TITLE);

            String artist =
                    tag.getFirst(FieldKey.ARTIST);

            String album =
                    tag.getFirst(FieldKey.ALBUM);

            if (!title.isBlank()) {
                currentTitle = title;
            }

            if (!artist.isBlank()) {
                currentArtist = artist;
            }

            if (!album.isBlank()) {
                currentAlbum = album;
            }

            Artwork artwork =
                    tag.getFirstArtwork();

            if (artwork != null &&
                    artwork.getBinaryData() != null) {

                currentArtwork =
                        new Image(
                                new ByteArrayInputStream(
                                        artwork.getBinaryData()
                                )
                        );
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public String getTitle(File file) {
        return getSongMetadata(file).title;
    }

    public String getArtist(File file) {
        return getSongMetadata(file).artist;
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

        if (isVlcSong()) {

            long duration =
                    vlcManager.getDuration();

            vlcManager.seek(
                    (long)(duration * percent)
            );

            return;
        }

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

        loadMetadata(file);

        currentSongIndex = songs.indexOf(file);

        try {
            try {

                if (file.getName().toLowerCase().endsWith(".mp3")) {

                    Mp3File mp3 = new Mp3File(file);

                    if (mp3.hasId3v2Tag()) {

                        ID3v2 id3v2 = mp3.getId3v2Tag();

                        if (id3v2.getArtist() != null && !id3v2.getArtist().isBlank()) {
                            currentArtist = id3v2.getArtist();
                        }
                        else
                        {
                            currentArtist = "Unknown Artist";
                        }
                        if (id3v2.getAlbum() != null && !id3v2.getAlbum().isBlank()) {

                            currentAlbum = id3v2.getAlbum();

                        } else {

                            currentAlbum = "Unknown Album";
                        }
                        if (id3v2.getTitle() != null
                                && !id3v2.getTitle().isBlank()) {

                            currentTitle = id3v2.getTitle();
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

            if (
                    file.getName().toLowerCase().endsWith(".flac")
                            ||
                            file.getName().toLowerCase().endsWith(".m4a")
            ) {
                if (mediaPlayer != null) {

                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }

                vlcManager.stop();
                LastSongManager.saveSong(file);
                vlcManager.play(file);




                if (songChangeListener != null) {
                    songChangeListener.onSongChanged(file);
                }

                return;
            }
            vlcManager.stop();
            Media media = new Media(file.toURI().toString());


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