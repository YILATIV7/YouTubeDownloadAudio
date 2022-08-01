
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String parseURL(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static void main(String[] args) {
        final String Y2MATE_URL = "https://www.y2mate.com/en324/youtube-mp3/";
        System.out.print("Type YouTube video url: ");
        String url = new Scanner(System.in).nextLine().strip();
        AudioDownloader.download(Y2MATE_URL + parseURL(url));
    }
}
