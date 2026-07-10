package dev.aman.auramusicfx;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    // Prevent the logger from being garbage collected so the log level is
    // maintained
    private static final java.util.logging.Logger JAUDIOTAGGER_LOGGER = java.util.logging.Logger
            .getLogger("org.jaudiotagger");
    static {
        JAUDIOTAGGER_LOGGER.setLevel(java.util.logging.Level.OFF);
    }

    @Override
    public void start(Stage stage) {

        PlayerUI playerUI = new PlayerUI();

        playerUI.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}