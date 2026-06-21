package dev.aman.auramusicfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import java.io.File;


public class ControlsView {

    private Timeline vlcTimer;
    private Button playButton;

    private Button muteButton;
    private double lastVolume = 100;

    private Button previousButton;
    private Button nextButton;

    private VBox progressSection = new VBox(14);
    private ProgressBar volumeFill;
    private HBox controlsSection = new HBox(60);

    private HBox topBar = new HBox();
    private ArtworkSection artworkSection;
    private Slider progressBar;
    private Slider volumeSlider;
    private ProgressBar progressFill;

    private Label currentTime = new Label("0:00");
    private Label totalTime = new Label("-0:00");

    private HBox timeRow = new HBox();

    private void loadSongsRecursive(File folder, MediaManager mediaManager) {

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                loadSongsRecursive(file, mediaManager);

            } else {

                String name = file.getName().toLowerCase();

                if (
                        name.endsWith(".mp3")
                                || name.endsWith(".wav")
                                || name.endsWith(".m4a")
                                || name.endsWith(".flac")
                ) {

                    mediaManager.addSong(file);
                }
            }
        }
    }
    private void updateAccentColor(Color color) {

        String rgb = String.format(
                        "rgb(%d,%d,%d)",
                        (int)(color.getRed() * 255),
                        (int)(color.getGreen() * 255),
                        (int)(color.getBlue() * 255)
                );

        progressFill.setStyle(
                "-fx-accent: " + rgb + ";" +
                        "-fx-control-inner-background: rgba(255,255,255,0.15);"
        );

        volumeFill.setStyle(
                "-fx-accent: " + rgb + ";" +
                        "-fx-control-inner-background: rgba(255,255,255,0.15);"
        );
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

        this.artworkSection = artworkSection;
        this.playlistView = playlistView;
        this.mediaManager = mediaManager;
        this.lyricsManager = lyricsManager;
        this.lyricsView = lyricsView;

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
        vlcTimer = new Timeline(
                new KeyFrame(
                        Duration.millis(100),
                        e -> {

                            if (mediaManager.isVlcSong()) {

                                long duration =
                                        mediaManager
                                                .getVlcManager()
                                                .getDuration();

                                long current =
                                        mediaManager
                                                .getVlcManager()
                                                .getCurrentTime();
                                PlaybackPositionManager.savePosition(
                                        current / 1000.0
                                );

                                if (duration > 0) {

                                    double percent =
                                            (current * 100.0)
                                                    / duration;

                                    progressBar.setValue(percent);

                                    progressFill.setProgress(
                                            current / (double) duration
                                    );

                                    currentTime.setText(
                                            formatTime(current / 1000.0)
                                    );

                                    totalTime.setText(
                                            "-" +
                                                    formatTime(
                                                            (duration - current)
                                                                    / 1000.0
                                                    )
                                    );
                                }

                                lyricsManager.updateLyrics(
                                        current / 1000.0,
                                        lyricsView.getLyricsBox(),
                                        lyricsView.getScrollPane()
                                );
                            }

                        }
                )
        );


        vlcTimer.setCycleCount(Timeline.INDEFINITE);

        vlcTimer.play();
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            VolumeManager.saveVolume(newVal.doubleValue());
            volumeFill.setProgress(newVal.doubleValue() / 100.0);
            if (newVal.doubleValue() == 0) {

                muteButton.setText("🔇");

            } else {

                muteButton.setText("🔊");
            }

            if (mediaManager.isVlcSong()) {

                mediaManager.getVlcManager().setVolume((int) newVal.doubleValue());
            } else if (mediaManager.getMediaPlayer() != null) {
                mediaManager.getMediaPlayer().setVolume(newVal.doubleValue() / 100.0);
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
        artworkSection
                .dominantColorProperty()
                .addListener((obs, oldColor, color) -> {

                    updateAccentColor(color);

                });
    }
    public void loadFolder(File folder) {

        if (folder == null) {
            return;
        }

        LastFolderManager.saveFolder(folder);

        mediaManager.clearSongs();

        loadSongsRecursive(
                folder,
                mediaManager
        );

        if (mediaManager.getSongs().isEmpty()) {
            return;
        }

        File firstSong =
                mediaManager.getSongs().get(0);

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
                mediaManager.getCurrentTitle()
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

        playlistView.loadSongs(
                mediaManager.getSongs(),
                mediaManager,
                artworkSection,
                lyricsManager,
                lyricsView
        );

        playlistView.setActiveSong(firstSong);

        playButton.setText("❚❚");
    }
    public void openSongFromExplorer(File selectedSong) {

        if (selectedSong == null || !selectedSong.exists()) {
            return;
        }

        File folder =
                selectedSong.getParentFile();

        loadFolder(folder);
        LastFolderManager.saveFolder(selectedSong.getParentFile());
        LastSongManager.saveSong(selectedSong);
        mediaManager.playSong(selectedSong);

        artworkSection.setArtwork(
                mediaManager.getCurrentArtwork()
        );

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
                selectedSong,
                lyricsView.getLyricsBox()
        );

        playlistView.setActiveSong(
                selectedSong
        );

        playButton.setText("❚❚");
    }
    private void refreshSeekbar() {
        progressBar.applyCss();
        progressBar.layout();
    }
    private final MediaManager mediaManager;
    private final LyricsManager lyricsManager;
    private final LyricsView lyricsView;
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
        Button aboutButton =
                new Button("ⓘ");
        aboutButton.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.18);

    -fx-background-radius: 18;

    -fx-font-size: 15px;

    -fx-font-weight: bold;

    -fx-min-width: 36;
    -fx-min-height: 36;
""");
        aboutButton.setOnAction(e -> {


            Stage aboutStage = new Stage();
            final double[] offsetX = new double[1];
            final double[] offsetY = new double[1];
            VBox root = new VBox(16);
            root.setPrefWidth(320);

            root.setAlignment(Pos.CENTER);

            root.setPadding(new Insets(24));
            root.setOnMousePressed(event -> {

                offsetX[0] = event.getSceneX();
                offsetY[0] = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {

                aboutStage.setX(
                        event.getScreenX()
                                - offsetX[0]
                );

                aboutStage.setY(
                        event.getScreenY()
                                - offsetY[0]
                );
            });
            root.setOnMouseClicked(event -> {

                if (event.getClickCount() == 2) {

                    aboutStage.close();
                }
            });
            root.setStyle("""
    -fx-background-color:
        rgba(20,20,20,0.78);

    -fx-background-radius: 28;

    -fx-border-color:
        rgba(255,255,255,0.22);

    -fx-border-width: 1.5;

    -fx-border-radius: 28;

    -fx-effect:
        dropshadow(
            gaussian,
            rgba(0,0,0,0.45),
            30,
            0.3,
            0,
            8
        );
""");
            Label title = new Label("🎵 Aura Music");
            Label version = new Label("Version 2.0");
            version.setStyle("""
    -fx-text-fill:
        rgba(255,255,255,0.65);

    -fx-font-size: 13px;
""");
            title.setStyle("""
    -fx-text-fill: white;
    -fx-font-size: 22px;
    -fx-font-weight: bold;
""");
            Label info = new Label("""
Built with

• JavaFX
• VLCJ
• Jaudiotagger

Features

• Lyrics
• Artwork
• Favorites
• Queue
• FLAC / ALAC
• Resume Playback
• Volume Memory
• Keyboard Shortcuts

© Built By Aman
""");
            info.setStyle("""
    -fx-text-fill:
        rgba(255,255,255,0.85);

    -fx-font-size: 14px;
""");
            info.setWrapText(true);

            info.setMaxWidth(260);

            info.setAlignment(Pos.CENTER);

            info.setTextAlignment(
                    javafx.scene.text.TextAlignment.CENTER
            );

            Label hint = new Label("Double-click anywhere to close");
            hint.setStyle("""
    -fx-text-fill:
        rgba(255,255,255,0.55);

    -fx-font-size: 12px;

    -fx-font-style: italic;
""");

            root.getChildren().addAll(
                    title,
                    version,
                    info,
                    hint
            );

            Scene scene = new Scene(root);

            scene.setFill(Color.TRANSPARENT);

            aboutStage.initStyle(
                    StageStyle.TRANSPARENT
            );

            aboutStage.setScene(scene);
            aboutStage.focusedProperty().addListener(
                    (obs, oldVal, focused) -> {

                        if (!focused) {

                            aboutStage.close();
                        }
                    }
            );

            aboutStage.showAndWait();
        });
        closeButton.setOnAction(e -> {

            try {

                if (mediaManager.isVlcSong()) {

                    mediaManager.getVlcManager().stop();

                } else if (mediaManager.getMediaPlayer() != null) {

                    mediaManager.getMediaPlayer().stop();
                }

            } catch (Exception ex) {

                ex.printStackTrace();
            }

            javafx.application.Platform.exit();
            mediaManager.getVlcManager().shutdown();
            System.exit(0);
        });
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

            chooser.setTitle("Select Music Folder");

            File folder =
                    chooser.showDialog(stage);

            loadFolder(folder);
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
                aboutButton,
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
                        if (mediaManager.getMediaPlayer() == null) {
                            return;
                        }{

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

        double savedVolume = VolumeManager.loadVolume();
        volumeSlider.setValue(savedVolume);
        volumeFill.setProgress(volumeSlider.getValue() / 100.0);
        if (mediaManager.isVlcSong()) {
            mediaManager.getVlcManager().setVolume((int) savedVolume);

        } else if (mediaManager.getMediaPlayer() != null) {
            mediaManager.getMediaPlayer().setVolume(savedVolume / 100.0);
        }
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
        muteButton = new Button("🔊");
        if (volumeSlider.getValue() == 0) {

            muteButton.setText("🔇");

        } else {

            muteButton.setText("🔊");
        }
        muteButton.setStyle("""
    -fx-background-color: transparent;
    -fx-font-size: 18px;
""");

        muteButton.setOnAction(e -> {

            if (volumeSlider.getValue() > 0) {

                lastVolume =
                        volumeSlider.getValue();

                volumeSlider.setValue(0);

                muteButton.setText("🔇");

            } else {

                volumeSlider.setValue(lastVolume);

                muteButton.setText("🔊");
            }
        });

        volumeRow.setAlignment(
                Pos.CENTER
        );

        volumeRow.getChildren().addAll(
                muteButton,
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

        artworkSection.setSongTitle(mediaManager.getCurrentTitle()
        );
        artworkSection.setArtist(
                mediaManager.getCurrentArtist()
        );
        artworkSection.setAlbum(
                mediaManager.getCurrentAlbum()
        );
        String name =
                song.getName().toLowerCase();

        String[] info =
                mediaManager.getAudioInfo(song);

        artworkSection.setQuality(
                info[0],
                info[1]
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
        playlistView.setActiveSong(song);
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

            if (mediaManager.isVlcSong()) {

                if (mediaManager.getVlcManager().isPlaying()) {

                    mediaManager.getVlcManager().pause();
                    playButton.setText("▶");

                } else {

                    mediaManager.getVlcManager().resume();
                    playButton.setText("❚❚");
                }

                mediaManager.syncWindowsPlaybackStatus();
                return;
            }

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
                        mediaManager.getCurrentTitle()
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
                playlistView.setActiveSong(previousSong);
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
                AnimationManager.pulse(
                        artworkSection.getArtwork()
                );
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
                        nextSong,
                        lyricsView.getLyricsBox()
                );
                playlistView.setActiveSong(nextSong);
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

        File folder = LastFolderManager.loadFolder();

        if (folder == null) {
            return;
        }





        mediaManager.clearSongs();
        loadSongsRecursive(
                folder,
                mediaManager
        );

        if (mediaManager.getSongs().isEmpty()) {
            return;
        }


        if (!mediaManager.getSongs().isEmpty()) {

            File songToPlay = LastSongManager.loadSong();

            if (songToPlay == null || !mediaManager.getSongs().contains(songToPlay)) {

                songToPlay = mediaManager.getSongs().get(0);
            }
            final File finalSongToPlay = songToPlay;

            Platform.runLater(() -> {

                mediaManager.playSong(finalSongToPlay);
                mediaManager.restorePosition();
            });


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
                    mediaManager.getCurrentTitle()
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

            playlistView.setActiveSong(songToPlay);

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

    public Button getNextButton() {
        return nextButton;
    }

    public Button getPreviousButton() {
        return previousButton;
    }

    public void togglePlayPause() {
        playButton.fire();
    }

    public void playNext() {
        nextButton.fire();
    }

    public void playPrevious() {
        previousButton.fire();
    }

    public void toggleMute() {
        muteButton.fire();
    }

    public void focusSearch() {
        playlistView.getSearchField().requestFocus();
    }
    public javafx.scene.control.TextField getSearchField() {
        return playlistView.getSearchField();
    }

}
