package dev.aman.auramusicfx;

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

        File song = getCurrentSong();

        File lastSong = LastSongManager.loadSong();

        if (song == null
                || lastSong == null
                || !song.equals(lastSong)) {
            return;
        }

        double savedPosition = PlaybackPositionManager.loadPosition();

        new Thread(() -> {

            try {

                Thread.sleep(1500);

                vlcManager.seek((long) (savedPosition * 1000));

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

            AudioFile audioFile = AudioFileIO.read(file);

            var header = audioFile.getAudioHeader();

            int sampleRate = Integer.parseInt(
                    header.getSampleRate());

            String format;

            if ((file.getName().toLowerCase().endsWith(".flac")
                    || file.getName().toLowerCase().endsWith(".m4a"))
                    && sampleRate > 48000) {

                format = "Hi-Res Lossless";

            } else if (file.getName().toLowerCase().endsWith(".flac")
                    || file.getName().toLowerCase().endsWith(".m4a")) {

                format = "Lossless";

            } else if (header.getFormat()
                    .toLowerCase()
                    .contains("mp3")) {

                format = "MP3";

            } else {

                format = header.getFormat();
            }

            double khz = sampleRate / 1000.0;

            String details;

            if (file.getName().toLowerCase().endsWith(".m4a")) {

                details = String.format(
                        "%.0f kHz • ALAC",
                        khz);

            } else if (file.getName().toLowerCase().endsWith(".flac")) {

                details = String.format(
                        "%.1f kHz • FLAC",
                        khz);

            } else {

                details = header.getBitRate()
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

    private RepeatMode repeatMode = RepeatMode.ALL;

    public void cycleRepeatMode() {

        switch (repeatMode) {

            case OFF ->
                repeatMode = RepeatMode.ALL;

            case ALL ->
                repeatMode = RepeatMode.ONE;

            case ONE ->
                repeatMode = RepeatMode.OFF;
        }
    }

    public RepeatMode getRepeatMode() {

        return repeatMode;
    }

    private final VlcMediaManager vlcManager = new VlcMediaManager();

    public VlcMediaManager getVlcManager() {
        return vlcManager;
    }

    private Image currentArtwork;
    private File currentArtworkFile;

    private String currentArtist = "Unknown Artist";
    private String currentAlbum = "Unknown Album";

    public String getCurrentArtist() {
        return currentArtist;
    }

    public String getCurrentAlbum() {
        return currentAlbum;
    }
    
    public File getCurrentArtworkFile() {
        return currentArtworkFile;
    }

    private void saveArtworkToFile(byte[] data) {
        try {
            if (currentArtworkFile != null && currentArtworkFile.exists()) {
                currentArtworkFile.delete();
            }
            currentArtworkFile = File.createTempFile("auramusicfx_artwork", ".jpg");
            currentArtworkFile.deleteOnExit();
            java.nio.file.Files.write(currentArtworkFile.toPath(), data);
        } catch (Exception ex) {
            ex.printStackTrace();
            currentArtworkFile = null;
        }
    }

    private void loadMetadata(File file) {

        try {

            AudioFile audioFile = AudioFileIO.read(file);

            Tag tag = audioFile.getTag();

            if (tag == null) {
                return;
            }

            String title = tag.getFirst(FieldKey.TITLE);

            String artist = tag.getFirst(FieldKey.ARTIST);

            String album = tag.getFirst(FieldKey.ALBUM);

            if (!title.isBlank()) {
                currentTitle = title;
            }

            if (!artist.isBlank()) {
                currentArtist = artist;
            }

            if (!album.isBlank()) {
                currentAlbum = album;
            }

            Artwork artwork = tag.getFirstArtwork();

            if (artwork != null &&
                    artwork.getBinaryData() != null) {

                byte[] data = artwork.getBinaryData();
                saveArtworkToFile(data);
                
                currentArtwork = new Image(
                        new ByteArrayInputStream(data),
                        800, 800, true, true);
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

    private final List<SongChangeListener> songChangeListeners = new ArrayList<>();

    public void addSongChangeListener(SongChangeListener listener) {
        songChangeListeners.add(listener);
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

    public void clearSongs() {

        songs.clear();
    }

    public void seek(double percent) {
        long duration = vlcManager.getDuration();
        vlcManager.seek((long) (duration * percent));
    }

    public void playSong(File file) {
        currentArtwork = null;
        currentArtworkFile = null;

        String fileName = file.getName();
        int extensionIndex = fileName.lastIndexOf('.');
        currentTitle = extensionIndex > 0
                ? fileName.substring(0, extensionIndex)
                : fileName;
        currentArtist = "Unknown Artist";
        currentAlbum = "Unknown Album";

        loadMetadata(file);
        int index = songs.indexOf(file);
        if (index < 0) {
            songs.add(file);
            index = songs.size() - 1;
        }
        currentSongIndex = index;
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
                        saveArtworkToFile(imageData);
                        currentArtwork = new Image(
                                new ByteArrayInputStream(imageData),
                                800, 800, true, true);
                    }
                }
            }
        } catch (Exception ex) {

            System.out.println(
                    "Cannot play: "
                            + file.getName());

            ex.printStackTrace();
        }

        try {
            vlcManager.stop();
            LastSongManager.saveSong(file);
            vlcManager.play(file);

            for (SongChangeListener listener : songChangeListeners) {
                listener.onSongChanged(file);
            }
        } catch (Exception ex) {
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setHeaderText("Playback Error");
                alert.setContentText(
                        file.getName() + "\n\nAn error occurred while playing this file.");
                alert.showAndWait();
            });
        }
    }

    // interface

    public interface SongChangeListener {

        void onSongChanged(File song);
    }

    public File getCurrentSong() {

        if (songs.isEmpty()) {
            return null;
        }

        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) {
            return null;
        }

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

                randomIndex = (int) (Math.random() * songs.size());

            } while (songs.size() > 1 &&
                    randomIndex == currentSongIndex);

            currentSongIndex = randomIndex;

        } else {

            currentSongIndex++;

            if (currentSongIndex >= songs.size()) {

                currentSongIndex = 0;
            }
        }

        return songs.get(currentSongIndex);
    }

    public void pause() {
        vlcManager.pause();
    }

    public void resume() {
        vlcManager.resume();
    }

    public void togglePlayPause() {
        if (vlcManager.isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    public boolean isPlaying() {
        return vlcManager.isPlaying();
    }

    public double getCurrentDurationSeconds() {
        return vlcManager.getDuration() / 1000.0;
    }

    public double getCurrentPositionSeconds() {
        return vlcManager.getCurrentTime() / 1000.0;
    }

    public void seekToSeconds(double seconds) {
        vlcManager.seek((long) (seconds * 1000));
    }

}
