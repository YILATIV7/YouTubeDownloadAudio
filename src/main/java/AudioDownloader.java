import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class AudioDownloader {

    public static void download(String URL) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        try {
            HtmlPage page = webClient.getPage(URL);

            // -- Step 1: Click on first button --
            HtmlButton dwnButton;
            do {
                Thread.sleep(1000);
                dwnButton = page.querySelector("button.btn.btn-success.btn-mp3");
            } while (dwnButton == null);
            page.executeJavaScript("document.querySelector('button.btn.btn-success.btn-mp3').click();");

            // -- Step 2: Click on second button --
            HtmlAnchor aButton;
            do {
                Thread.sleep(1000);
                aButton = page.querySelector("a.btn.btn-success.btn-file:not([id])");
            } while (aButton == null);

            WebWindow window = page.getEnclosingWindow();
            page.executeJavaScript("document.querySelectorAll('a.btn.btn-success.btn-file')[1].click();");
            UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();

            // -- Step 3: Download --
            InputStream initialStream = downloadPage.getInputStream();
            File targetFile = new File("src/main/resources/a.mp3");

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
