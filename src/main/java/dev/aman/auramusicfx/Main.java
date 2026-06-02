package dev.aman.auramusicfx;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        PlayerUI playerUI = new PlayerUI();

        playerUI.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}