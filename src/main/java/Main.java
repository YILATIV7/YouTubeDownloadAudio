import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String parseURL(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url); //url is youtube url for which you want to extract the id.
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static void main(String[] args) {
        final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=HdWrAZP_svk&ab_channel=%D0%A1%D0%B3%D0%BD%D0%B8%D0%B2%D0%BD%D0%B8%D0%BA%D0%B0";
        final String Y2MATE_URL = "https://www.y2mate.com/en324/youtube-mp3/";
        final String url = Y2MATE_URL + parseURL(YOUTUBE_VIDEO_URL);

        // ------------------------------

        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        // Configure
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        try {
            HtmlPage page = webClient.getPage(url);



            // -- Step 3 --

            HtmlButton dwnButton;
            do {
                Thread.sleep(1000);
                dwnButton = page.querySelector("button.btn.btn-success.btn-mp3");
            } while (dwnButton == null);
            System.out.println("------------------" + dwnButton.getTextContent());

            page.executeJavaScript("document.querySelector('button.btn.btn-success.btn-mp3').click();");


            // -- Step 4 --

            HtmlAnchor aButton;
            do {
                Thread.sleep(1000);
                List<DomNode> nodes = page.querySelectorAll("a.btn.btn-success.btn-file");

                if (nodes.size() > 1 && nodes.get(1) != null) {
                    aButton = (HtmlAnchor) nodes.get(1);
                    break;
                }

            } while (true);
            System.out.println("------------------" + aButton.getTextContent());

            WebWindow window = page.getEnclosingWindow();
            page.executeJavaScript("document.querySelectorAll('a.btn.btn-success.btn-file')[1].click();");
            UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();

            InputStream initialStream = downloadPage.getInputStream();
            File targetFile = new File("src/main/java/a.mp3");

            java.nio.file.Files.copy(
                    initialStream,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);


            // -- Closing --

            webClient.getCurrentWindow().getJobManager().removeAllJobs();
            webClient.close();

        } catch (IOException | InterruptedException e) {
            System.out.println("An error occurred: " + e);
        }
    }
}
