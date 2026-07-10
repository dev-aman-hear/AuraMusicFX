package dev.aman.auramusicfx;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Button;

import java.io.File;
import java.util.Objects;

public class PlayerUI {

    // Fields
    private double xOffset;
    private double yOffset;
    private VBox playerScreen;
    private VBox lyricsScreen;
    private VBox queueScreen;
    private Button lyricsBtn;
    private Button queueBtn;

    private VBox createControlsOverlay(ControlsView controlsView) {
        VBox controlsOverlay = new VBox(12);
        StackPane.setAlignment(controlsOverlay, Pos.BOTTOM_CENTER);
        controlsOverlay.setPadding(new Insets(0, 0, 24, 0));
        controlsOverlay.getChildren().addAll(controlsView.getProgressSection(), controlsView.getControlsSection());
        controlsOverlay.setVisible(true);
        controlsOverlay.setManaged(true);
        controlsOverlay.setOpacity(1);
        return controlsOverlay;
    }

    // Navigation Methods
    private void showPlayer() {

        playerScreen.setVisible(true);
        playerScreen.setManaged(true);

        lyricsScreen.setVisible(false);
        lyricsScreen.setManaged(false);

        queueScreen.setVisible(false);
        queueScreen.setManaged(false);

        updateNavigationState();
    }

    private void showLyrics() {
        if (lyricsScreen.isVisible()) {
            showPlayer();
            return;
        }
        playerScreen.setVisible(false);
        playerScreen.setManaged(false);

        lyricsScreen.setVisible(true);
        lyricsScreen.setManaged(true);

        queueScreen.setVisible(false);
        queueScreen.setManaged(false);

        updateNavigationState();
    }

    private void showQueue() {
        if (queueScreen.isVisible()) {
            showPlayer();
            return;
        }
        playerScreen.setVisible(false);
        playerScreen.setManaged(false);

        lyricsScreen.setVisible(false);
        lyricsScreen.setManaged(false);

        queueScreen.setVisible(true);
        queueScreen.setManaged(true);

        updateNavigationState();
    }

    private void updateNavigationState() {

        String normal = """
                    -fx-background-color:
                        rgba(0,0,0,0.28);

                    -fx-background-radius: 20;

                    -fx-text-fill: white;

                    -fx-font-size: 14px;

                    -fx-font-weight: bold;

                    -fx-padding: 10 22 10 22;
                """;

        String active = """
                    -fx-background-color: white;

                    -fx-background-radius: 20;

                    -fx-text-fill: #202020;

                    -fx-font-size: 14px;

                    -fx-font-weight: bold;

                    -fx-padding: 10 22 10 22;

                    -fx-effect:
                        dropshadow(
                            gaussian,
                            rgba(255,255,255,0.6),
                            16,
                            0.4,
                            0,
                            0
                        );
                """;

        lyricsBtn.setStyle(normal);
        queueBtn.setStyle(normal);

        if (lyricsScreen.isVisible()) {
            lyricsBtn.setStyle(active);
        }

        if (queueScreen.isVisible()) {
            queueBtn.setStyle(active);
        }
    }

    private final MiniPlayerView miniPlayerView = new MiniPlayerView();
    private final LyricsHeaderView lyricsHeaderView = new LyricsHeaderView();

    // UI Builders
    private ControlsView controlsView;

    private void buildPlayerScreen(HBox topOverlay, ArtworkSection artworkSection, VBox controlsOverlay) {
        playerScreen.getChildren().addAll(
                topOverlay,
                artworkSection.getView(),
                controlsOverlay);
    }

    private void buildLyricsScreen(LyricsHeaderView lyricsHeaderView, LyricsView lyricsView) {
        lyricsScreen.setPadding(new Insets(80, 20, 24, 20));
        lyricsScreen.getChildren().addAll(lyricsHeaderView.getView(), lyricsView.getView());
    }

    private void buildQueueScreen(PlaylistView playlistView) {
        queueScreen.setAlignment(Pos.TOP_CENTER);
        playlistView.getView().prefWidthProperty().bind(queueScreen.widthProperty());
        queueScreen.setPadding(new Insets(80, 20, 24, 20));
        queueScreen.setVisible(false);
        queueScreen.setManaged(false);
        queueScreen.getChildren().addAll(miniPlayerView.getView(), playlistView.getView());
    }

    private Scene createScene(
            StackPane root,
            VBox controlsOverlay,
            ControlsView controlsView) {
        Scene scene = new Scene(root, 420, 760);
        scene.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });
        scene.setOnDragDropped(event -> {

            var db = event.getDragboard();
            if (db.hasFiles()) {
                File dropped = db.getFiles().get(0);
                if (dropped.isDirectory()) {
                    controlsView.loadFolder(dropped);
                } else {
                    controlsView.loadFolder(
                            dropped.getParentFile());
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });
        // CREATE FADE ANIMATIONS
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), controlsOverlay);
        fadeOut.setToValue(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), controlsOverlay);
        fadeIn.setToValue(1);
        PauseTransition idle = new PauseTransition(Duration.seconds(4));
        idle.setOnFinished(e -> {
            fadeOut.play();
        });
        // SHOW UI ON MOUSE MOVE
        root.setOnMouseMoved(e -> {
            fadeOut.stop();
            controlsOverlay.setOpacity(1);
            controlsOverlay.setVisible(true);
            controlsOverlay.setManaged(true);
            fadeIn.play();
            idle.playFromStart();
        });

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private HBox createBottomNavigation(Runnable lyricsAction, Runnable queueAction) {

        lyricsBtn = new Button("Lyrics");
        queueBtn = new Button("Queue");
        String navStyle = """
                    -fx-background-color:
                        rgba(255,255,255,0.55);

                    -fx-background-radius: 20;

                    -fx-text-fill: #202020;

                    -fx-font-size: 14px;

                    -fx-font-weight: bold;

                    -fx-padding: 10 22 10 22;
                """;
        lyricsBtn.setStyle(navStyle);
        queueBtn.setStyle(navStyle);

        lyricsBtn.setOnAction(e -> lyricsAction.run());
        queueBtn.setOnAction(e -> queueAction.run());

        HBox nav = new HBox(16);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(10));

        nav.setStyle("-fx-background-color: transparent;");

        nav.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        nav.setPickOnBounds(false);
        nav.getChildren().addAll(
                lyricsBtn,
                queueBtn);

        return nav;
    }

    private HBox createTopOverlay(ControlsView controlsView) {
        HBox topOverlay = new HBox();
        topOverlay.setAlignment(Pos.CENTER);
        topOverlay.getChildren().add(controlsView.getTopBar());

        return topOverlay;
    }

    private ImageView createBackgroundImage() {
        ImageView backgroundImage = new ImageView();
        backgroundImage.setPreserveRatio(false);
        backgroundImage.setOpacity(0.75);
        backgroundImage.setEffect(new GaussianBlur(120));

        backgroundImage.setCache(true);
        backgroundImage.setCacheHint(javafx.scene.CacheHint.SPEED);

        return backgroundImage;
    }

    // BINDING
    private void bindArtworkData(ArtworkSection artworkSection) {

        // MINIPLAYER

        miniPlayerView.getArtwork().imageProperty().bind(
                artworkSection.getArtwork().imageProperty());

        miniPlayerView.getTitle().textProperty().bind(
                artworkSection.getSongTitleLabel().textProperty());

        miniPlayerView.getArtist().textProperty().bind(
                artworkSection.getArtistLabel().textProperty());

        // LYRICHEADER
        lyricsHeaderView.getArtwork().imageProperty().bind(
                artworkSection.getArtwork().imageProperty());
        lyricsHeaderView.getTitle().textProperty().bind(
                artworkSection.getSongTitleLabel().textProperty());
        lyricsHeaderView.getArtist().textProperty().bind(artworkSection.getArtistLabel().textProperty());
        // END
    }

    private void initializeScreens() {
        playerScreen = new VBox(12);
        lyricsScreen = new VBox(20);
        queueScreen = new VBox(20);

        playerScreen.setAlignment(Pos.TOP_CENTER);
        playerScreen.setPadding(new Insets(28));
        playerScreen.setMaxHeight(720);
        playerScreen.setMaxWidth(420);
    }

    private void setupMiniPlayer() {
        miniPlayerView.getView().setPadding(new Insets(10, 20, 10, 20));
        miniPlayerView.getView().setMaxWidth(Double.MAX_VALUE);
    }

    private void buildScreens(ArtworkSection artworkSection, LyricsView lyricsView,
            PlaylistView playlistView, HBox topOverlay, VBox controlsOverlay) {
        buildPlayerScreen(topOverlay, artworkSection, controlsOverlay);
        buildLyricsScreen(lyricsHeaderView, lyricsView);
        buildQueueScreen(playlistView);
    }

    private final ArtworkSection artworkSection = new ArtworkSection();
    private final LyricsView lyricsView = new LyricsView();
    private final PlaylistView playlistView = new PlaylistView();

    private ControlsView createControlsView(ArtworkSection artworkSection, LyricsView lyricsView,
            PlaylistView playlistView, Stage stage) {
        return new ControlsView(
                mediaManager,
                artworkSection,
                lyricsManager,
                lyricsView,
                playlistView,
                stage);
    }

    private void openSongFromExplorer() {

        if (Launcher.startupSong == null) {
            return;
        }

        File song = new File(
                Launcher.startupSong);

        if (!song.exists()) {
            return;
        }

        controlsView.openSongFromExplorer(
                song);

    }

    private void setupBackgroundBindings(ImageView backgroundImage, Scene scene, ArtworkSection artworkSection) {
        backgroundImage.fitWidthProperty().bind(scene.widthProperty());
        backgroundImage.fitHeightProperty().bind(scene.heightProperty());
        artworkSection.bindBackground(backgroundImage);
    }

    private void setupStage(Stage stage, Scene scene) {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("AuraMusicFX");
        stage.setScene(scene);
        stage.show();
        Platform.runLater(this::openSongFromExplorer);
        scene.getRoot().requestFocus();
    }

    private final MediaManager mediaManager = new MediaManager();
    private SMTCManager smtcManager;
    private final LyricsManager lyricsManager = new LyricsManager();

    private Pane createGlassOverlay() {

        Pane overlay = new Pane();

        overlay.setStyle("""
                    -fx-background-color:
                        linear-gradient(
                            to bottom,
                            rgba(255,255,255,0.10),
                            rgba(255,255,255,0.03)
                        );

                    -fx-background-radius: 28;
                """);

        return overlay;
    }

    // START
    public void start(Stage stage) {

        stage.setOnCloseRequest(event -> {
            try {
                mediaManager.getVlcManager().stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit();
            mediaManager.getVlcManager().shutdown();
            System.exit(0);
        });

        StackPane root = new StackPane();

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());

        clip.setArcWidth(36);
        clip.setArcHeight(36);

        root.setClip(clip);
        ImageView backgroundImage = createBackgroundImage();

        Pane overlay = createGlassOverlay();
        artworkSection.dominantColorProperty().addListener((obs, oldColor, color) -> {

            String css = String.format(
                    "-fx-background-color: rgba(%d,%d,%d,0.25);",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            overlay.setStyle(css);
        });

        initializeScreens();

        lyricsManager.setMediaManager(mediaManager);
        ControlsView controlsView = createControlsView(artworkSection, lyricsView, playlistView, stage);

        this.controlsView = controlsView;

        smtcManager = new SMTCManager(mediaManager, controlsView);
        smtcManager.init();

        controlsView.autoLoadLastFolder(mediaManager, artworkSection, lyricsManager, lyricsView);

        bindArtworkData(artworkSection);

        HBox topOverlay = createTopOverlay(controlsView);
        topOverlay.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topOverlay.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        HBox bottomNavigation = createBottomNavigation(this::showLyrics, this::showQueue);
        updateNavigationState();
        StackPane.setAlignment(bottomNavigation, Pos.BOTTOM_CENTER);
        bottomNavigation.setPadding(new Insets(0, 0, 20, 0));

        VBox controlsOverlay = createControlsOverlay(controlsView);

        setupMiniPlayer();

        buildScreens(artworkSection, lyricsView, playlistView, topOverlay, controlsOverlay);
        root.getChildren().addAll(backgroundImage, overlay, lyricsScreen, queueScreen, playerScreen, bottomNavigation);

        showPlayer();

        Scene scene = createScene(
                root,
                controlsOverlay,
                controlsView);

        scene.setOnKeyPressed(e -> {

            switch (e.getCode()) {

                case SPACE -> {
                    controlsView.togglePlayPause();
                    e.consume();
                }

                case RIGHT -> {
                    if (!e.isControlDown()) {
                        return;
                    }
                    controlsView.playNext();
                    e.consume();
                }

                case LEFT -> {
                    if (!e.isControlDown()) {
                        return;
                    }
                    controlsView.playPrevious();
                    e.consume();
                }

                case M -> {
                    if (!e.isControlDown()) {
                        return;
                    }
                    controlsView.toggleMute();
                    e.consume();
                }

                case F -> {

                    if (e.isControlDown()) {

                        controlsView.focusSearch();

                        controlsView.getSearchField()
                                .selectAll();

                        e.consume();
                    }
                }
            }
        });

        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,
                event -> {

                    switch (event.getCode()) {

                        case N -> {

                            if (!event.isControlDown()) {
                                return;
                            }

                            controlsView.getNextButton().fire();
                            event.consume();
                        }
                        case P -> {

                            if (!event.isControlDown()) {
                                return;
                            }

                            controlsView.getPreviousButton().fire();
                            event.consume();
                        }

                        case L -> {
                            if (!event.isControlDown()) {
                                return;
                            }
                            showLyrics();
                            scene.getRoot().requestFocus();
                            event.consume();
                        }

                        case Q -> {
                            if (!event.isControlDown()) {
                                return;
                            }
                            showQueue();
                            scene.getRoot().requestFocus();
                            event.consume();
                        }

                        case S -> {

                            mediaManager.toggleShuffle();
                            playlistView.refreshButtons();
                            event.consume();
                        }

                        case R -> {

                            mediaManager.cycleRepeatMode();
                            playlistView.refreshButtons();
                            event.consume();
                        }
                    }
                });

        setupBackgroundBindings(backgroundImage, scene, artworkSection);

        setupStage(stage, scene);
        stage.getIcons().add(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream(
                                        "/icons/AuraMusicFX_Ultimate.ico"))));
    }
}
