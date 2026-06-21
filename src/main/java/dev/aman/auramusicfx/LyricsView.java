package dev.aman.auramusicfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LyricsView {

    private VBox lyricsBox =
            new VBox(16);

    private ScrollPane scrollPane =
            new ScrollPane();

    private StackPane root =
            new StackPane();

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public LyricsView() {

        lyricsBox.setAlignment(
                Pos.CENTER
        );

        lyricsBox.setPadding(
                new Insets(260, 0, 260, 0)
        );

        scrollPane.setPrefHeight(580);
        scrollPane.setContent(lyricsBox);

        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(false);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setStyle("""
            -fx-background:
                transparent;

            -fx-background-color:
                transparent;
        """);

        root.getChildren().add(
                scrollPane
        );
    }

    public VBox getLyricsBox() {
        return lyricsBox;
    }

    public StackPane getView() {

        return root;
    }

}
