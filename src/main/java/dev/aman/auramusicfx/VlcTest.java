import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

public class VlcTest {

    public static void main(String[] args) {

        AudioPlayerComponent player =
                new AudioPlayerComponent();

        player.mediaPlayer().media().play(
                "E:\\Apple Music\\Lossless\\02 Aap Se Milkar.m4a"
        );

        System.out.println("Playing...");

        try {
            Thread.sleep(60000);
        } catch (Exception ignored) {
        }
    }
}