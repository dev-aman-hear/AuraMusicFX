package dev.aman.auramusicfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import javafx.scene.paint.Color;

public class ArtworkSection {

    private VBox root = new VBox(10);

    private ImageView artwork = new ImageView();
    Rectangle clip = new Rectangle();



    public Label getArtistLabel() {
        return artist;
    }
    private Color extractColor(Image image) {

        BufferedImage img = SwingFXUtils.fromFXImage(image, null);

        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = 0; y < img.getHeight(); y += 8) {
            for (int x = 0; x < img.getWidth(); x += 8) {

                int rgb = img.getRGB(x, y);

                r += (rgb >> 16) & 0xff;
                g += (rgb >> 8) & 0xff;
                b += rgb & 0xff;

                count++;
            }
        }

        return Color.rgb((int)(r / count), (int)(g / count), (int)(b / count));
    }
    private ObjectProperty<Color> dominantColor = new SimpleObjectProperty<>(Color.PINK);

    public ObjectProperty<Color> dominantColorProperty() {
        return dominantColor;
    }

    private Label songTitle = new Label("No song playing");

    private Label artist = new Label("Artist");
    private Label album = new Label("Album");
    public void setAlbum(String albumName) {
        album.setText(albumName);
    }
    public Label getAlbumLabel() {
        return album;
    }
    public Label getSongTitleLabel() {
        return songTitle;
    }
    public ArtworkSection() {
        Pane overlay = new Pane();
        overlay.setStyle("""
    -fx-background-color:
        rgba(255,255,255,0.10);
""");

        root.setAlignment(Pos.CENTER);
        artwork.setImage(
                new Image(
                        "https://upload.wikimedia.org/wikipedia/en/2/26/Blurred_Lines_cover.png"
                )
        );

        artwork.setFitWidth(260);
        artwork.setFitHeight(260);
        artwork.setPreserveRatio(false);

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(
                artwork.fitWidthProperty()
        );

        clip.heightProperty().bind(
                artwork.fitHeightProperty()
        );

        clip.setArcWidth(50);
        clip.setArcHeight(50);

        artwork.setClip(clip);

        artwork.setClip(clip);
        artwork.setStyle("""
    -fx-effect:
        dropshadow(
            gaussian,
            rgba(0,0,0,0.35),
            40,
            0.3,
            0,
            12
        );
""");
        songTitle.setStyle("""
    -fx-font-size: 24px;

    -fx-font-weight: bold;

    -fx-text-fill: white;

    -fx-font-family: "Segoe UI";

    -fx-padding: 12 0 0 0;
""");
        artist.setStyle("""
    -fx-font-size: 15px;

    -fx-font-weight: 500;

    -fx-text-fill:
        rgba(255,255,255,0.68);

    -fx-font-family: "Segoe UI";
""");
        album.setStyle("""
    -fx-font-size: 15px;

    -fx-font-weight: 500;

    -fx-text-fill:
        rgba(255,255,255,0.68);

    -fx-font-family: "Segoe UI";
""");

        root.getChildren().addAll(
                artwork,
                songTitle,
                artist,
                album
        );
    }

    public void setArtwork(Image image) {
        dominantColor.set(extractColor(image));
        artwork.setImage(image);
    }

    public VBox getView() {
        return root;
    }

    public ImageView getArtwork() {
        return artwork;
    }

    public void setSongTitle(String title) {
        songTitle.setText(title);
    }

    public void setArtist(String artistName) {
        artist.setText(artistName);
    }
    public void bindBackground(ImageView background) {
        background.imageProperty().bind(
                artwork.imageProperty()
        );
    }
}