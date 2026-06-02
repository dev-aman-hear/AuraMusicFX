package dev.aman.auramusicfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class ControlsView {

    private Button playButton;

    private Button previousButton;
    private Button nextButton;

    private VBox progressSection = new VBox(14);
    private ProgressBar volumeFill;
    private HBox controlsSection = new HBox(60);

    private HBox topBar = new HBox();

    private Slider progressBar;
    private Slider volumeSlider;
    private ProgressBar progressFill;

    private Label currentTime = new Label("0:00");

    private Label totalTime = new Label("-0:00");

    private HBox timeRow = new HBox();

    private String cleanName(File song) {

        if (song == null) {
            return "";
        }

        String name = song.getName();

        int dot = name.lastIndexOf('.');

        if (dot > 0) {
            return name.substring(0, dot);
        }

        return name;
    }

    private void reconnectListeners(
            MediaManager mediaManager,
            LyricsManager lyricsManager,
            LyricsView lyricsView
    ) {

        setupProgressUpdates(mediaManager);

        mediaManager.setLyricsListener(
                (obs, oldTime, newTime) -> {

                    lyricsManager.updateLyrics(
                            newTime.toSeconds(),
                            lyricsView.getLyricsBox(),
                            lyricsView.getScrollPane()
                    );
                }
        );
    }

    public ControlsView(
            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView,
            PlaylistView playlistView,
            Stage stage
    ){
        this.playlistView = playlistView;

        buildTopBar(
                mediaManager,
                artworkSection,
                lyricsManager,
                lyricsView,
                playlistView,
                stage
        );


        buildProgress(mediaManager);

        buildControls(

                mediaManager,

                artworkSection,

                lyricsManager,

                lyricsView
        );
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {

            volumeFill.setProgress(
                    newVal.doubleValue() / 100.0
            );

            if (mediaManager.getMediaPlayer() != null) {

                mediaManager.getMediaPlayer()
                        .setVolume(newVal.doubleValue() / 100.0);
            }
        });

        //find constructor

        mediaManager.setSongChangeListener(song -> {

            updateNowPlaying(

                    song,

                    mediaManager,

                    artworkSection,

                    lyricsManager,

                    lyricsView
            );
        });

    }private void refreshSeekbar() {
        progressBar.applyCss();
        progressBar.layout();
    }
    private final PlaylistView playlistView;
    private void buildTopBar(
            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView,
            PlaylistView playlistView,
            Stage stage
    ) {

        Button openButton =
                new Button("Open Folder");

        openButton.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.20);

    -fx-background-radius: 20;

    -fx-font-size: 14px;

    -fx-font-weight: bold;

    -fx-padding: 8 18 8 18;
""");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                javafx.scene.layout.Priority.ALWAYS
        );

        Button minimizeButton =
                new Button("—");
        minimizeButton.setOnAction(e ->

                stage.setIconified(true)
        );
        minimizeButton.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.18);

    -fx-background-radius: 18;

    -fx-font-size: 15px;

    -fx-font-weight: bold;

    -fx-min-width: 36;
    -fx-min-height: 36;
""");
        Button closeButton =
                new Button("✕");

        closeButton.setOnAction(e ->
                stage.close()
        );
        closeButton.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.18);

    -fx-background-radius: 18;

    -fx-font-size: 15px;

    -fx-font-weight: bold;

    -fx-min-width: 36;
    -fx-min-height: 36;
""");

        openButton.setOnAction(e -> {

            DirectoryChooser chooser =
                    new DirectoryChooser();

            chooser.setTitle(
                    "Select Music Folder"
            );

            File folder =
                    chooser.showDialog(stage);

            if (folder != null) {

                LastFolderManager.saveFolder(folder);
                mediaManager.clearSongs();

                File[] files =
                        folder.listFiles();

                if (files != null) {

                    for (File file : files) {

                        String name =
                                file.getName()
                                        .toLowerCase();

                        if (
                                name.endsWith(".mp3")
                                        || name.endsWith(".wav")
                                        || name.endsWith(".m4a")
                        ) {

                            mediaManager.addSong(file);
                        }
                    }

                    File firstSong = null;


                        if (!mediaManager.getSongs().isEmpty()) {

                            firstSong = mediaManager
                                    .getSongs()
                                    .get(0);

                            mediaManager.playSong(firstSong);
                            refreshSeekbar();
                            reconnectListeners(
                                    mediaManager,
                                    lyricsManager,
                                    lyricsView
                            );


                            artworkSection.setArtwork(
                                    mediaManager.getCurrentArtwork()
                            );

                            artworkSection.setSongTitle(
                                    cleanName(firstSong)
                            );
                            artworkSection.setArtist(
                                    mediaManager.getCurrentArtist()
                            );
                            artworkSection.setAlbum(
                                    mediaManager.getCurrentAlbum()
                            );
                            lyricsManager.loadLyrics(
                                    firstSong,
                                    lyricsView.getLyricsBox()
                            );
                            playButton.setText("❚❚");
                        }


                    playlistView.loadSongs(
                            mediaManager.getSongs(),
                            mediaManager,
                            artworkSection,
                            lyricsManager,
                            lyricsView
                    );
                    playlistView.setActiveSong(
                            cleanName(firstSong)
                    );
                }
            }
        });

        topBar.setAlignment(Pos.CENTER);

        topBar.setPrefWidth(360);

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        topBar.getChildren().addAll(

                openButton,

                spacer,

                minimizeButton,

                closeButton
        );
        topBar.setPadding(
                new Insets(0, 6, 0, 6)
        );
    }
    private void setupProgressUpdates(
            MediaManager mediaManager
    ) {

        if (mediaManager.getMediaPlayer() == null) {
            return;
        }
        mediaManager.getMediaPlayer()
                .currentTimeProperty()
                .addListener((obs, oldTime, newTime) -> {
                    if (
                            !progressBar.isPressed()
                    )
                    {

                        double total =
                                mediaManager
                                        .getMediaPlayer()
                                        .getTotalDuration()
                                        .toSeconds();

                        double current =
                                newTime.toSeconds();

                        double target =
                                (current / total) * 100;

                        progressBar.setValue(target);

                        progressFill.setProgress(current / total);
                        currentTime.setText(
                                formatTime(current)
                        );

                        double remaining =
                                total - current;

                        totalTime.setText(
                                "-" + formatTime(remaining)
                        );

                    }

                });
    }
    private String formatTime(double seconds) {

        int mins =
                (int) seconds / 60;

        int secs =
                (int) seconds % 60;

        return String.format(
                "%d:%02d",
                mins,
                secs
        );
    }
    private void buildProgress(MediaManager mediaManager) {
        progressFill = new ProgressBar(0);

        progressBar = new Slider();

        volumeSlider = new Slider();

        StackPane seekStack = new StackPane();

        seekStack.setAlignment(Pos.CENTER);

        volumeSlider.setValue(100);

        volumeSlider.setMinWidth(220);
        volumeSlider.setPrefWidth(220);
        volumeSlider.setMaxWidth(220);

        progressBar.setMinWidth(340);
        progressBar.setPrefWidth(340);
        progressBar.setMaxWidth(340);
        progressFill.setPrefWidth(340);
        progressFill.setMaxWidth(340);

        progressFill.setStyle("""
    -fx-accent: white;

    -fx-control-inner-background:
        rgba(255,255,255,0.18);

    -fx-background-radius: 999;
""");
        progressBar.setStyle("""
    -fx-control-inner-background: transparent;

    -fx-pref-height: 4;

    -fx-background-color: transparent;
""");
        seekStack.getChildren().addAll(
                progressFill,
                progressBar
        );
        currentTime =
                new Label("0:00");

        totalTime =
                new Label("-0:00");
        volumeFill = new ProgressBar(1.0);
        volumeFill.setPrefWidth(220);

        volumeFill.setStyle("""
    -fx-accent: white;

    -fx-control-inner-background:
        rgba(255,255,255,0.18);

    -fx-background-radius: 999;
""");

        StackPane volumeStack = new StackPane();

        volumeStack.getChildren().addAll(
                volumeFill,
                volumeSlider
        );

        currentTime.setStyle("""
    -fx-text-fill:
        rgba(255,255,255,0.55);

    -fx-font-size: 13px;

    -fx-font-weight: bold;
""");

        totalTime.setStyle("""
    -fx-text-fill:
        rgba(255,255,255,0.55);

    -fx-font-size: 13px;

    -fx-font-weight: bold;
""");
        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        timeRow.getChildren().addAll(

                currentTime,

                spacer,

                totalTime
        );
        HBox volumeRow = new HBox(12);
        volumeRow.setAlignment(Pos.CENTER);

        Label volumeIcon = new Label("♪");

        volumeRow.setAlignment(
                Pos.CENTER
        );

        volumeRow.getChildren().addAll(
                volumeIcon,
                volumeStack
        );
        progressSection.setSpacing(16);
        timeRow.setAlignment(Pos.CENTER);
        timeRow.setMaxWidth(340);
        timeRow.setPrefWidth(340);

        progressBar.valueChangingProperty()

                .addListener((obs, wasChanging, changing) -> {

                    if (!changing) {

                        double percent =
                                progressBar.getValue()
                                        / 100.0;

                        mediaManager.seek(percent);
                    }
                });
        progressBar.setOnMousePressed(e -> {

            double percent =
                    e.getX()
                            / progressBar.getWidth();

            percent =
                    Math.max(
                            0,
                            Math.min(1, percent)
                    );

            progressBar.setValue(
                    percent * 100
            );

            mediaManager.seek(percent);
        });
        progressBar.setOnMouseDragged(e -> {

            double percent =
                    e.getX()
                            / progressBar.getWidth();

            percent =
                    Math.max(
                            0,
                            Math.min(1, percent)
                    );

            progressBar.setValue(
                    percent * 100
            );

            double total =
                    mediaManager
                            .getMediaPlayer()
                            .getTotalDuration()
                            .toSeconds();

            double current =
                    total * percent;

            currentTime.setText(
                    formatTime(current)
            );
        });
        progressBar.setOnMouseReleased(e -> {

            double percent =
                    progressBar.getValue()
                            / 100.0;

            mediaManager.seek(percent);
        });
        progressBar.setOnMouseEntered(e -> {

            progressBar.setScaleY(1.15);
        });

        progressBar.setOnMouseExited(e -> {
            progressBar.setScaleY(1);
        });

        progressSection.setAlignment(Pos.CENTER);
        progressSection.getChildren().addAll(

                seekStack,
                timeRow,
                volumeRow
        );
    }
//formate method


    private void updateNowPlaying(
            File song,
            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView
    ) {

        artworkSection.setArtwork(

                mediaManager.getCurrentArtwork()
        );

        artworkSection.setSongTitle(
                cleanName(song)
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
        reconnectListeners(
                mediaManager,
                lyricsManager,
                lyricsView
        );

        playButton.setText("❚❚");
        playlistView.setActiveSong(
                cleanName(song)
        );
    }
    private void buildControls(

            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView
    ){
        previousButton =
                new Button("❮");

        playButton =
                new Button("▶");

        nextButton =
                new Button("❯");

        playButton.setMinWidth(90);
        playButton.setPrefWidth(90);
        playButton.setMaxWidth(90);

        playButton.setAlignment(Pos.CENTER);


        previousButton.setStyle("""
    -fx-background-color: transparent;
    -fx-font-size: 28px;
    -fx-font-family: "Segoe UI Symbol";
    -fx-text-fill: rgba(0,0,0,0.82);
""");

        playButton.setStyle("""
    -fx-background-color: transparent;
    -fx-font-size: 44px;
    -fx-font-family: "Segoe UI Symbol";
    -fx-text-fill: rgba(0,0,0,0.82);
    -fx-padding: 0;
""");

        nextButton.setStyle("""
    -fx-background-color: transparent;
    -fx-font-size: 28px;
    -fx-font-family: "Segoe UI Symbol";
    -fx-text-fill: rgba(0,0,0,0.82);
""");

        setupHover(previousButton);
        setupHover(playButton);
        setupHover(nextButton);

        playButton.setOnAction(e -> {

            boolean currentlyPlaying =
                    mediaManager.isPlaying();

            mediaManager.togglePlayPause();

            if (currentlyPlaying) {

                playButton.setText("▶");

            } else {

                playButton.setText("❚❚");
            }
        });

        previousButton.setOnAction(e -> {

            File previousSong =
                    mediaManager.previousSong();

            if (previousSong != null) {

                mediaManager.playSong(previousSong);

                reconnectListeners(
                        mediaManager,
                        lyricsManager,
                        lyricsView
                );

                refreshSeekbar();
                artworkSection.setArtwork(
                        mediaManager.getCurrentArtwork()
                );

                artworkSection.setSongTitle(
                        cleanName(previousSong)
                );
                artworkSection.setArtist(
                        mediaManager.getCurrentArtist()
                );
                artworkSection.setAlbum(
                        mediaManager.getCurrentAlbum()
                );
                lyricsManager.loadLyrics(
                        previousSong,
                        lyricsView.getLyricsBox()
                );

                playButton.setText("❚❚");
            }
        });


        nextButton.setOnAction(e -> {

            File nextSong =
                    mediaManager.nextSong();

            if (nextSong != null) {

                mediaManager.playSong(nextSong);

                reconnectListeners(
                        mediaManager,
                        lyricsManager,
                        lyricsView
                );

                refreshSeekbar();

                artworkSection.setArtwork(
                        mediaManager.getCurrentArtwork()
                );

                artworkSection.setSongTitle(
                        cleanName(nextSong)
                );
                artworkSection.setArtist(
                        mediaManager.getCurrentArtist()
                );
                artworkSection.setAlbum(
                        mediaManager.getCurrentAlbum()
                );
                lyricsManager.loadLyrics(
                        nextSong,
                        lyricsView.getLyricsBox()
                );

                playButton.setText("❚❚");
            }
        });



        controlsSection.setAlignment(
                Pos.CENTER
        );
        controlsSection.getChildren().addAll(

                previousButton,

                playButton,

                nextButton
        );
    }
    public void autoLoadLastFolder(
            MediaManager mediaManager,
            ArtworkSection artworkSection,
            LyricsManager lyricsManager,
            LyricsView lyricsView
    ) {

        File folder =
                LastFolderManager.loadFolder();

        if (folder == null) {
            return;
        }

        mediaManager.clearSongs();

        File[] files =
                folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            String name =
                    file.getName().toLowerCase();

            if (
                    name.endsWith(".mp3")
                            || name.endsWith(".wav")
                            || name.endsWith(".m4a")
            ) {

                mediaManager.addSong(file);
            }
        }

        if (!mediaManager.getSongs().isEmpty()) {

            File songToPlay =
                    LastSongManager.loadSong();

            if (songToPlay == null
                    || !mediaManager.getSongs().contains(songToPlay)) {

                songToPlay =
                        mediaManager.getSongs().get(0);
            }

            mediaManager.playSong(songToPlay);

            refreshSeekbar();

            reconnectListeners(
                    mediaManager,
                    lyricsManager,
                    lyricsView
            );

            artworkSection.setArtwork(
                    mediaManager.getCurrentArtwork()
            );

            artworkSection.setSongTitle(
                    cleanName(songToPlay)
            );

            artworkSection.setArtist(
                    mediaManager.getCurrentArtist()
            );
            artworkSection.setAlbum(mediaManager.getCurrentAlbum()
            );

            lyricsManager.loadLyrics(
                    songToPlay,
                    lyricsView.getLyricsBox()
            );

            playlistView.loadSongs(
                    mediaManager.getSongs(),
                    mediaManager,
                    artworkSection,
                    lyricsManager,
                    lyricsView
            );

            playlistView.setActiveSong(
                    cleanName(songToPlay)
            );

            playButton.setText("❚❚");
        }
    }

    private void setupHover(Button button) {
        button.setOnMouseEntered(e -> {
            button.setOpacity(0.7);
            button.setScaleX(1.08);
            button.setScaleY(1.08);
        });

        button.setOnMouseExited(e -> {

            button.setOpacity(1);
            button.setScaleX(1);
            button.setScaleY(1);
        });
    }

    public VBox getProgressSection() {
        return progressSection;
    }

    public HBox getControlsSection() {
        return controlsSection;
    }

    public HBox getTopBar() {
        return topBar;
    }

    public Button getPlayButton() {
        return playButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

}