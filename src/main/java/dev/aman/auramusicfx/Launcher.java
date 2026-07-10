package dev.aman.auramusicfx;

public class Launcher {
    public static String startupSong = null;

    public static void main(String[] args) {
        if (args.length > 0) {
            startupSong = args[0];
        }
        Main.main(args);
    }
}
