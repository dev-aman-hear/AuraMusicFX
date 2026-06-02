package dev.aman.auramusicfx;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MiniPlayerView {

    private final HBox root;

    private final ImageView artwork;
    private final Label title;
    private final Label artist;

    public MiniPlayerView() {

        root = new HBox(12);
        root.setAlignment(Pos.CENTER_LEFT);

        artwork = new ImageView();
        artwork.setFitWidth(56);
        artwork.setFitHeight(56);
        artwork.setPreserveRatio(false);

        title = new Label("No song");
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        artist = new Label("Artist");
        artist.setStyle("""
            -fx-text-fill: rgba(255,255,255,0.72);
            -fx-font-size: 14px;
        """);

        VBox textBox = new VBox(4);
        textBox.getChildren().addAll(
                title,
                artist
        );

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