package dev.aman.auramusicfx;

public class Launcher {

    public static String startupSong;

    public static void main(String[] args) {

        if (args.length > 0) {

            startupSong = args[0];

            System.out.println(
                    "Startup song = "
                            + startupSong
            );
        }

        Main.main(args);
    }
}