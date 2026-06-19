package dev.aman.auramusicfx;

public class LyricLine {

    private final double time;

    private final String text;

    public LyricLine(
            double time,
            String text
    ) {

        this.time = time;
        this.text = text;
    }

    public double getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
}