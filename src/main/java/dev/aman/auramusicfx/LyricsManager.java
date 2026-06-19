package dev.aman.auramusicfx;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LyricsManager {

    private MediaManager mediaManager;

    private List<LyricLine> lyrics =
            new ArrayList<>();
    private int lastActiveIndex = -1;
    private Timeline scrollTimeline;

    public List<LyricLine> getLyrics() {
        return lyrics;
    }

    public void setMediaManager(
            MediaManager mediaManager
    ) {
        this.mediaManager = mediaManager;
    }

    public void loadLyrics(
            File songFile,
            VBox lyricsBox
    ) {

        lyrics.clear();
        lyricsBox.getChildren().clear();
        lastActiveIndex = -1;

        try {

            String path =
                    songFile.getAbsolutePath();

            int dot =
                    path.lastIndexOf('.');

            if (dot == -1) {
                return;
            }

            String lrcPath =
                    path.substring(0, dot)
                            + ".lrc";

            File lrcFile =
                    new File(lrcPath);

            if (!lrcFile.exists()) {
                return;
            }

            List<String> lines =
                    Files.readAllLines(
                            lrcFile.toPath()
                    );

            for (String line : lines) {

                if (line.startsWith("[")) {

                    int end =
                            line.indexOf("]");

                    String timeText =
                            line.substring(1, end);

                    String lyricText =
                            line.substring(end + 1);

                    String[] parts =
                            timeText.split(":");

                    double minutes =
                            Double.parseDouble(parts[0]);

                    double seconds =
                            Double.parseDouble(parts[1]);

                    double totalSeconds =
                            minutes * 60 + seconds;

                    lyrics.add(
                            new LyricLine(
                                    totalSeconds,
                                    lyricText
                            )
                    );

                    Label lyricLabel =
                            new Label(lyricText);

                    double seekTime =
                            totalSeconds;

                    lyricLabel.setOnMouseClicked(e -> {

                        if (mediaManager == null) {
                            return;
                        }

                        if (mediaManager.isVlcSong()) {

                            mediaManager
                                    .getVlcManager()
                                    .seek((long)(seekTime * 1000));

                        } else if (mediaManager.getMediaPlayer() != null) {

                            mediaManager.getMediaPlayer()
                                    .seek(
                                            Duration.seconds(seekTime)
                                    );
                        }
                    });

                    lyricLabel.setOnMouseEntered(e -> {

                        lyricLabel.setScaleX(1.05);
                        lyricLabel.setScaleY(1.05);
                    });

                    lyricLabel.setOnMouseExited(e -> {

                        lyricLabel.setScaleX(1);
                        lyricLabel.setScaleY(1);
                    });

                    lyricLabel.setWrapText(true);

                    lyricLabel.setMaxWidth(320);

                    lyricLabel.setStyle("""
                                -fx-font-size: 28px;
                            
                                -fx-font-weight: bold;
                            
                                -fx-text-fill:
                                    rgba(255,255,255,0.28);
                            """);

                    lyricsBox.getChildren().add(
                            lyricLabel
                    );
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public void updateLyrics(
            double currentSeconds,
            VBox lyricsBox,
            ScrollPane scrollPane
    ) {

        int activeIndex = -1;

        for (int i = 0; i < lyrics.size(); i++) {
            if (currentSeconds >= lyrics.get(i).getTime()) {
                activeIndex = i;
            }
        }

        if (activeIndex == lastActiveIndex) {
            return;
        }
        lastActiveIndex = activeIndex;

        for (int i = 0; i < lyrics.size(); i++) {
            Label label = (Label) lyricsBox.getChildren().get(i);

            if (i == activeIndex) {
                double contentHeight = lyricsBox.getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double y = label.getBoundsInParent().getMinY();
                double target = (y + label.getHeight() / 2 - viewportHeight / 2) / (contentHeight - viewportHeight);
                target = Math.max(0, Math.min(1, target));
                if (scrollTimeline != null) {
                    scrollTimeline.stop();
                }
                scrollTimeline = new Timeline(new KeyFrame(Duration.millis(450),
                        new KeyValue(scrollPane.vvalueProperty(), target)));
                scrollTimeline.play();

                label.setStyle("""
                            -fx-font-size: 32px;
                            -fx-font-weight: bold;
                            -fx-text-fill: white;
                            -fx-effect:
                                dropshadow(
                                    gaussian,
                                    rgba(255,255,255,0.20),
                                    18, 0.2, 0,0 );
                        """);
            } else {
                int distance = Math.abs(i - activeIndex);
                double opacity;

                if (distance == 1) {
                    opacity = 0.55;
                } else if (distance == 2) {
                    opacity = 0.35;
                } else {
                    opacity = 0.18;
                }

                label.setStyle("""
                            -fx-font-size: 26px;
                            -fx-font-weight: bold;
                            -fx-text-fill:
                                rgba(255,255,255,%f);
                            -fx-effect: null;
                        """.formatted(opacity));
            }
        }
    }
}