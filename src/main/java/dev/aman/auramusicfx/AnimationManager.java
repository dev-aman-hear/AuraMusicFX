package dev.aman.auramusicfx;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationManager {

    public static void pulse(Node node) {

        ScaleTransition pulse =
                new ScaleTransition(
                        Duration.millis(220),
                        node
                );

        pulse.setFromX(0.96);
        pulse.setFromY(0.96);

        pulse.setToX(1.0);
        pulse.setToY(1.0);

        pulse.play();
    }
}