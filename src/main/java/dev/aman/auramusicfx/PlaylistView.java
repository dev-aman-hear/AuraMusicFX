package dev.aman.auramusicfx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistView {

    private static final ExecutorService metadataLoader = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            runnable -> {
                Thread thread = new Thread(runnable, "PlaylistMetadataLoader");
                thread.setDaemon(true);
                return thread;
            }
    );

    private final Map<File, Button> songButtons = new HashMap<>();

    public TextField getSearchField() {
        return searchField;
    }

    public void refreshButtons() {
        updateButtonStates();
    }
    private final java.util.Set<String> favorites = FavoritesManager.loadFavorites();

    private Button favoritesButton;
    private boolean showingFavorites = false;
    private void updateButtonStates() {

        if (mediaManager == null) {
            return;
        }

        if (mediaManager.isShuffleEnabled()) {

            shuffleButton.setText("🔀 ON");

            shuffleButton.setStyle("""
        -fx-background-color:
            rgba(255,255,255,0.85);

        -fx-background-radius: 18;

        -fx-text-fill: #202020;

        -fx-font-size: 14px;

        -fx-font-weight: bold;

        -fx-padding: 10 18 10 18;

        -fx-border-color:
            rgba(255,255,255,1.0);

        -fx-border-width: 2;

        -fx-border-radius: 18;
    """);

        } else {

            shuffleButton.setText("🔀 OFF");

            shuffleButton.setStyle(
                    glassButtonStyle()
            );
        }


        if (mediaManager.getRepeatMode()
                == MediaManager.RepeatMode.OFF) {

            repeatButton.setStyle(
                    glassButtonStyle()
            );

        } else {

            repeatButton.setStyle("""
        -fx-background-color:
            rgba(255,255,255,0.42);

        -fx-background-radius: 16;

        -fx-text-fill: #202020;

        -fx-font-weight: bold;

        -fx-padding: 10 16 10 16;

        -fx-effect:
            dropshadow(
                gaussian,
                rgba(255,255,255,0.6),
                16,
                0.3,
                0,
                0
            );
    """);
        }


        switch (mediaManager.getRepeatMode()) {

            case OFF ->
                    repeatButton.setText("🔁 OFF");

            case ALL ->
                    repeatButton.setText("🔁 ALL");

            case ONE ->
                    repeatButton.setText("🔂 ONE");
        }
    }
    private VBox songsBox = new VBox(12);
    private MediaManager mediaManager;
    private ScrollPane root = new ScrollPane();
    private VBox rootContainer;
    private Button activeButton;

    private TextField searchField;

    private Button shuffleButton;

    private Button repeatButton;


    private String hoverStyle() {
        return """
        -fx-background-color:
         rgba(255,255,255,0.35);

        -fx-background-radius: 12;

        -fx-text-fill: white;

        -fx-font-size: 17px;

        -fx-font-weight: bold;

        -fx-alignment: CENTER_LEFT;

      -fx-padding: 10 16 10 16;
    """;
    }

    private String glassButtonStyle() {

        return """
        -fx-background-color:
            rgba(255,255,255,0.22);

        -fx-background-radius: 28;

        -fx-border-color:
            rgba(255,255,255,0.45);

        -fx-border-width: 1.2;

        -fx-border-radius: 28;

        -fx-text-fill: white;

        -fx-font-size: 15px;

        -fx-font-weight: bold;

        -fx-padding: 14 22 14 22;

        -fx-effect:
            dropshadow(
                gaussian,
                rgba(255,255,255,0.15),
                15,
                0.2,
                0,
                0
            );
        """;
    }

    public PlaylistView() {


        searchField = new TextField();
        searchField.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.55);

    -fx-background-radius: 18;

    -fx-text-fill: #222222;

    -fx-prompt-text-fill:
        rgba(40,40,40,0.65);

    -fx-font-size: 14px;

    -fx-font-weight: bold;

    -fx-padding: 10 14 10 14;

    -fx-border-color:
        rgba(255,255,255,0.80);

    -fx-border-width: 1.5;

    -fx-border-radius: 18;

    -fx-effect:
        dropshadow(
            gaussian,
            rgba(255,255,255,0.7),
            15,
            0.3,
            0,
            0
        );
""");

        shuffleButton = new Button("🔀 OFF");

        shuffleButton.setPrefSize(115, 52);

        shuffleButton.setStyle(
                glassButtonStyle()
        );
        shuffleButton.setStyle("""
  -fx-background-color: rgba(255,255,255,0.85);
      -fx-border-width: 2;

    -fx-background-radius: 18;

    -fx-text-fill: #202020;

    -fx-font-size: 14px;

    -fx-font-weight: bold;

    -fx-padding: 10 18 10 18;

    -fx-border-color:
        rgba(255,255,255,0.9);

    -fx-border-radius: 18;
""");

        shuffleButton.setOnAction(e -> {

            mediaManager.toggleShuffle();

            updateButtonStates();
        });
        repeatButton = new Button("🔁 All");
        repeatButton.setPrefSize(115, 52);

        repeatButton.setStyle(
                glassButtonStyle()
        );

        repeatButton.setOnAction(e -> {

            mediaManager.cycleRepeatMode();

            updateButtonStates();

        });
        searchField.setPromptText("Search songs...");
        searchField.textProperty().addListener((obs, oldText, newText) -> {

            String search = newText.toLowerCase().trim();

            for (var node : songsBox.getChildren()) {

                Button button = (Button) node;

                String songName =
                        button.getText()
                                .replace("♪ ", "")
                                .replace("▶ ", "")
                                .toLowerCase();

                boolean visible =
                        songName.contains(search);

                button.setVisible(visible);
                button.setManaged(visible);
            }
        });
        HBox controlsRow = new HBox(16);
        controlsRow.setPadding(new Insets(5,0,5,0));
        controlsRow.setAlignment(Pos.CENTER);
        favoritesButton = new Button("♥ OFF");
        favoritesButton.setPrefSize(115, 52);
        favoritesButton.setStyle(glassButtonStyle());
        favoritesButton.setOnAction(e -> {

            showingFavorites = !showingFavorites;

            for (var node : songsBox.getChildren()) {

                Button button = (Button) node;

                boolean favorite = button.getText().startsWith("♥ ");

                boolean visible = !showingFavorites || favorite;

                button.setVisible(visible);
                button.setManaged(visible);
            }

            favoritesButton.setText(
                    showingFavorites
                            ? "♥ ON"
                            : "♥ OFF"
            );
        });

        controlsRow.getChildren().addAll(
                shuffleButton,
                favoritesButton,
                repeatButton

        );
        songsBox.setPadding(
                new Insets(12)
        );

        songsBox.setAlignment(
                Pos.TOP_LEFT
        );
        rootContainer = new VBox(12);

        rootContainer.getChildren().addAll(
                searchField,
                controlsRow,
                root
        );
        root.setContent(songsBox);
        root.setFitToWidth(true);
        root.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        root.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        root.setPrefHeight(520);

        root.setStyle("""
    -fx-background-color: transparent;
    -fx-background: transparent;
""");

    }
    private String normalStyle() {
        return """
        -fx-background-color:
            rgba(255,255,255,0.12);

        -fx-background-radius: 12;

        -fx-text-fill: white;

        -fx-font-size: 17px;

        -fx-font-weight: bold;

        -fx-alignment: CENTER_LEFT;

      -fx-padding: 10 16 10 16;
    """;
    }

    private String activeStyle() {
        return """
        -fx-background-color:
            rgba(255,255,255,0.38);
-fx-border-width: 2;

-fx-effect:
    dropshadow(
        gaussian,
        rgba(255,255,255,0.55),
        18,
        0.3,
        0,
        0
    );
        -fx-background-radius: 12;

        -fx-text-fill: white;

        -fx-font-size: 17px;

        -fx-font-weight: bold;

        -fx-alignment: CENTER_LEFT;

      -fx-padding: 10 16 10 16;

        -fx-border-color:
            rgba(255,255,255,0.45);

        -fx-border-radius: 12;
    """;
    }

    public void setActiveSong(File song) {

        Button button = songButtons.get(song);

        if (button == null) {
            return;
        }
        if (activeButton != null) {
            activeButton.setStyle(normalStyle());
            File oldSong = null;
            for (var entry : songButtons.entrySet()) {
                if (entry.getValue() == activeButton) {
                    oldSong = entry.getKey();
                    break;
                }
            }
            if (oldSong != null) {
                boolean wasFavorite = favorites.contains(oldSong.getAbsolutePath());
                String oldTitle = mediaManager.getTitle(oldSong);
                String oldArtist = mediaManager.getArtist(oldSong);
                activeButton.setText((wasFavorite ? "♥ " : "♪ ") + oldTitle + "\n" + oldArtist);
            } else {
                String oldName = activeButton.getText()
                        .replace("▶ ", "")
                        .replace("♥ ", "")
                        .replace("♪ ", "");
                activeButton.setText("♪ " + oldName);
            }
        }

        activeButton = button;
        activeButton.setStyle(activeStyle());
        String title = mediaManager.getTitle(song);
        String artist = mediaManager.getArtist(song);

        activeButton.setText(
                "▶ " + title
                        + "\n"
                        + artist
        );
        scrollToActiveSong();
    }

    public void loadSongs(
            List<File> songs,
            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView
    ) {
        this.mediaManager = mediaManager;
        songsBox.getChildren().clear();
        songButtons.clear();

        for (File song : songs) {

            String path = song.getAbsolutePath();

            boolean favorite = favorites.contains(path);

            String placeholderTitle = song.getName();
            int dotIdx = placeholderTitle.lastIndexOf('.');
            if (dotIdx > 0) {
                placeholderTitle = placeholderTitle.substring(0, dotIdx);
            }

            Button songButton = new Button(
                    (favorite ? "♥ " : "♪ ")
                            + placeholderTitle
                            + "\n"
                            + "Loading details..."
            );

            songButton.setWrapText(true);

            songButton.setAlignment(Pos.CENTER_LEFT);

            songButton.setPrefHeight(80);

            songButtons.put(song, songButton);
            songButton.setMaxWidth(Double.MAX_VALUE);
            songButton.setStyle(normalStyle());
            songButton.setOnMouseEntered(e -> {

                if (songButton != activeButton) {
                    songButton.setStyle(hoverStyle());
                }
            });

            songButton.setOnMouseExited(e -> {

                if (songButton == activeButton) {
                    songButton.setStyle(activeStyle());

                } else {
                    songButton.setStyle(normalStyle());
                }
            });

            songButton.setOnContextMenuRequested(e -> {
                String currentTitle = mediaManager.getTitle(song);
                String currentArtist = mediaManager.getArtist(song);
                if (favorites.contains(path)) {
                    favorites.remove(path);
                    if (songButton != activeButton) {
                        songButton.setText("♪ " + currentTitle + "\n" + currentArtist);
                    } else {
                        songButton.setText("▶ " + currentTitle + "\n" + currentArtist);
                    }
                } else {
                    favorites.add(path);
                    if (songButton != activeButton) {
                        songButton.setText("♥ " + currentTitle + "\n" + currentArtist);
                    } else {
                        songButton.setText("▶ " + currentTitle + "\n" + currentArtist);
                    }
                }

                FavoritesManager.saveFavorites(favorites);
            });

            songButton.setOnAction(e -> {
                if (activeButton != null) {

                    activeButton.setStyle(normalStyle());

                    File oldSong = null;
                    for (var entry : songButtons.entrySet()) {
                        if (entry.getValue() == activeButton) {
                            oldSong = entry.getKey();
                            break;
                        }
                    }
                    if (oldSong != null) {
                        boolean wasFavorite = favorites.contains(oldSong.getAbsolutePath());
                        String oldTitle = mediaManager.getTitle(oldSong);
                        String oldArtist = mediaManager.getArtist(oldSong);
                        activeButton.setText((wasFavorite ? "♥ " : "♪ ") + oldTitle + "\n" + oldArtist);
                    } else {
                        String oldName = activeButton.getText()
                                .replace("▶ ", "")
                                .replace("♥ ", "")
                                .replace("♪ ", "");
                        activeButton.setText("♪ " + oldName);
                    }
                }
                activeButton = songButton;
                String title = mediaManager.getTitle(song);
                String artist = mediaManager.getArtist(song);
                activeButton.setText(
                        "▶ " + title
                                + "\n"
                                + artist
                );
                activeButton.setStyle(activeStyle());
                scrollToActiveSong();

                mediaManager.playSong(song);

                artworkSection.setArtwork(mediaManager.getCurrentArtwork());

                artworkSection.setSongTitle(
                        mediaManager.getCurrentTitle()
                );

                artworkSection.setArtist(
                        mediaManager.getCurrentArtist()
                );

                artworkSection.setAlbum(
                        mediaManager.getCurrentAlbum()
                );

                lyricsManager.loadLyrics(

                        song,

                        lyricsView.getLyricsBox()
                );
            });
            songsBox.getChildren().add(songButton);

            metadataLoader.submit(() -> {
                String title = mediaManager.getTitle(song);
                String artist = mediaManager.getArtist(song);
                Platform.runLater(() -> {
                    if (songButtons.get(song) == songButton) {
                        if (songButton != activeButton) {
                            songButton.setText((favorite ? "♥ " : "♪ ") + title + "\n" + artist);
                        } else {
                            songButton.setText("▶ " + title + "\n" + artist);
                        }
                    }
                });
            });
        }

        updateButtonStates();
    }
    public VBox getView() {
        return rootContainer;
    }

    public VBox getSongsBox() {
        return songsBox;
    }
    public void scrollToActiveSong() {

        if (activeButton == null) {
            return;
        }

        double contentHeight =
                songsBox.getHeight();

        double viewportHeight =
                root.getViewportBounds()
                        .getHeight();

        double y =
                activeButton
                        .getBoundsInParent()
                        .getMinY();

        double target =
                (y - viewportHeight / 2)
                / (contentHeight - viewportHeight);

        target =
                Math.max(
                        0,
                        Math.min(1, target)
                );

        root.setVvalue(target);
    }
    public void setActiveSong(String songName) {

        for (var node : songsBox.getChildren()) {

            Button button = (Button) node;

            String text = button.getText()
                    .replace("♪ ", "")
                    .replace("▶ ", "")
                    .replaceFirst("[.][^.]+$", "");

            if (text.equalsIgnoreCase(songName)) {



                activeButton = button;

                activeButton.setStyle(
                        activeStyle()
                );

                activeButton.setText(
                        "▶ " + songName
                );

                scrollToActiveSong();

                break;
            }
        }
    }

}