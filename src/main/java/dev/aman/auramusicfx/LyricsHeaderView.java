package dev.aman.auramusicfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class LyricsHeaderView {

    private final HBox root;

    private final ImageView artwork;
    private final Label title;
    private final Label artist;

    public LyricsHeaderView() {

        root = new HBox(14);

        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(28,24,12,24));

        artwork = new ImageView();
        artwork.setFitWidth(70);
        artwork.setFitHeight(70);
        artwork.setPreserveRatio(false);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(artwork.fitWidthProperty());
        clip.heightProperty().bind(artwork.fitHeightProperty());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        artwork.setClip(clip);

        title = new Label();
        title.setStyle("""
            -fx-text-fill:white;
            -fx-font-size:24px;
            -fx-font-weight:bold;
        """);

        artist = new Label();
        artist.setStyle("""
            -fx-text-fill:rgba(255,255,255,0.72);
            -fx-font-size:16px;
        """);

        VBox textBox = new VBox(6);
        textBox.getChildren().addAll(title, artist);

        root.getChildren().addAll(
                artwork,
                textBox
        );
    }

    public HBox getView() {
        return root;
    }

    public ImageView getArtwork() {
        return artwork;
    }

    public Label getTitle() {
        return title;
    }

    public Label getArtist() {
        return artist;
    }
}