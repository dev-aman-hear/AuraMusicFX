package dev.aman.auramusicfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import java.io.File;
import java.util.List;

public class PlaylistView {
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

        -fx-padding: 12 16 12 16;
    """;
    }

    private String glassButtonStyle() {

        return """
       -fx-background-color:
       rgba(255,255,255,0.45);

        -fx-background-radius: 18;

        -fx-text-fill: #202020;

        -fx-font-size: 14px;

        -fx-font-weight: bold;

        -fx-padding: 10 18 10 18;

        -fx-border-color:
            rgba(255,255,255,0.75);

        -fx-border-radius: 18;
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

        shuffleButton.setMinWidth(120);
        shuffleButton.setPrefWidth(120);
        shuffleButton.setMaxWidth(120);
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
        repeatButton.setMinWidth(120);
        repeatButton.setPrefWidth(120);
        repeatButton.setMaxWidth(120);
        repeatButton.setStyle(
                glassButtonStyle()
        );

        repeatButton.setOnAction(e -> {

            mediaManager.cycleRepeatMode();

            updateButtonStates();

        });
        searchField.setPromptText("Search songs...");
        HBox controlsRow = new HBox(16);
        controlsRow.setPadding(new Insets(5,0,5,0));
        controlsRow.setAlignment(Pos.CENTER_LEFT);
        controlsRow.getChildren().addAll(
                shuffleButton,
                repeatButton
        );
        songsBox.setPadding(
                new Insets(12)
        );

        songsBox.setAlignment(
                Pos.TOP_LEFT
        );

        VBox container =
                new VBox(12);
        container.getChildren().addAll(searchField, controlsRow, songsBox);

        root.setContent(container);

        root.setFitToWidth(true);

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

        -fx-padding: 12 16 12 16;
    """;
    }

    private String activeStyle() {
        return """
        -fx-background-color:
            rgba(255,255,255,0.30);

        -fx-background-radius: 12;

        -fx-text-fill: white;

        -fx-font-size: 17px;

        -fx-font-weight: bold;

        -fx-alignment: CENTER_LEFT;

        -fx-padding: 12 16 12 16;

        -fx-border-color:
            rgba(255,255,255,0.45);

        -fx-border-radius: 12;
    """;
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

        for (File song : songs) {

            Button songButton =
                    new Button("♪ " + song.getName().replace(".mp3", "")
                    );

            songButton.setMaxWidth(Double.MAX_VALUE);

            songButton.setStyle(normalStyle());

            songButton.setOnMouseEntered(e -> {

                if (songButton != activeButton) {

                    songButton.setStyle(
                            hoverStyle()
                    );
                }
            });

            songButton.setOnMouseExited(e -> {

                if (songButton == activeButton) {

                    songButton.setStyle(
                            activeStyle()
                    );

                } else {

                    songButton.setStyle(
                            normalStyle()
                    );
                }
            });

            songButton.setOnAction(e -> {

                if (activeButton != null) {

                    activeButton.setStyle(
                            normalStyle()
                    );

                    String oldName =
                            activeButton.getText()
                                    .replace("▶ ", "");

                    activeButton.setText(
                            "♪ " + oldName
                    );
                }
                activeButton = songButton;
                activeButton.setText(
                        "▶ " +
                        song.getName().replace(".mp3", "")
                );

                activeButton.setStyle(
                        activeStyle()
                );
                scrollToActiveSong();

                mediaManager.playSong(song);

                artworkSection.setArtwork(
                        mediaManager.getCurrentArtwork()
                );

                artworkSection.setSongTitle(
                        song.getName()
                                .replace(".mp3", "")
                );


                lyricsManager.loadLyrics(

                        song,

                        lyricsView.getLyricsBox()
                );
            });
            songsBox.getChildren().add(songButton);
            updateButtonStates();
        }
    }

    public ScrollPane getView() {
        return root;
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

            String text =
                    button.getText()
                            .replace("♪ ", "")
                            .replace("▶ ", "");

            if (text.equals(songName)) {

                if (activeButton != null) {

                    activeButton.setStyle(
                            normalStyle()
                    );

                    activeButton.setText(
                            "♪ " +
                            activeButton.getText()
                                    .replace("▶ ", "")
                                    .replace("♪ ", "")
                    );
                }

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