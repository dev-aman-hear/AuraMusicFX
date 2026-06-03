package dev.aman.auramusicfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javafx.scene.paint.Color;

public class ArtworkSection {

    private VBox root = new VBox(10);
    private Label qualityFormat = new Label("FORMAT");

    private Label qualityDetails = new Label("DETAILS");

    public void setQuality(
            String format,
            String details
    ) {
        qualityFormat.setText(format);
        qualityDetails.setText(details);
    }

    private ImageView artwork = new ImageView();
    Rectangle clip = new Rectangle();



    public Label getArtistLabel() {
        return artist;
    }

    private Color extractColor(Image image) {

        if (image == null) {
            return Color.PINK;
        }

        BufferedImage img =
                SwingFXUtils.fromFXImage(image, null);

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

        return Color.rgb(
                (int)(r / count),
                (int)(g / count),
                (int)(b / count)
        );
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
        qualityFormat.setStyle("""
    -fx-font-size: 12px;
    -fx-font-weight: bold;
    -fx-text-fill: white;

    -fx-background-color:
        rgba(255,255,255,0.12);

    -fx-background-radius: 5;

    -fx-padding: 2 6 2 6;
""");

        qualityDetails.setStyle("""
    -fx-font-size: 10px;

    -fx-text-fill:
        rgba(255,255,255,0.55);
""");
        qualityFormat.setMaxWidth(
                Region.USE_PREF_SIZE
        );

        qualityDetails.setMaxWidth(
                Double.MAX_VALUE
        );

        root.setAlignment(Pos.CENTER);

        qualityDetails.setAlignment(
                Pos.CENTER
        );

        root.setAlignment(Pos.CENTER);
        artwork.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/default_artwork.png"))));

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
    -fx-font-size: 30px;

    -fx-font-weight: bold;

    -fx-text-fill: white;

    -fx-font-family: "Segoe UI";

    -fx-padding: 12 0 0 0;
""");
        artist.setStyle("""
    -fx-font-size: 16px;

    -fx-font-weight: 500;

    -fx-text-fill:
        rgba(255,255,255,0.75);

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
                qualityFormat,
                qualityDetails
        );
    }

    public void setArtwork(Image image) {

        Image newImage = image;

        if (newImage == null) {

            newImage = new Image(
                    "https://upload.wikimedia.org/wikipedia/en/2/26/Blurred_Lines_cover.png"
            );

            dominantColor.set(Color.PINK);

        } else {

            dominantColor.set(
                    extractColor(newImage)
            );
        }

        Color c = dominantColor.get();

        FadeTransition fadeOut =
                new FadeTransition(
                        Duration.millis(180),
                        artwork
                );

        fadeOut.setToValue(0);

        Image finalImage = newImage;

        fadeOut.setOnFinished(e -> {

            artwork.setImage(finalImage);

            artwork.setStyle(
                    String.format(
                            """
                            -fx-effect:
                                dropshadow(
                                    gaussian,
                                    rgba(%d,%d,%d,0.65),
                                    50,
                                    0.3,
                                    0,
                                    0
                                );
                            """,
                            (int)(c.getRed() * 255),
                            (int)(c.getGreen() * 255),
                            (int)(c.getBlue() * 255)
                    )
            );

            FadeTransition fadeIn =
                    new FadeTransition(
                            Duration.millis(250),
                            artwork
                    );

            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeIn.play();
        });

        fadeOut.play();
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