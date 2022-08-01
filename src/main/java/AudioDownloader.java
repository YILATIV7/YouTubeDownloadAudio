import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class AudioDownloader {
    static {
        // Disabling HTMLUnit logger
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
    }

    private static WebClient createWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setIncorrectnessListener((message, origin) -> {});
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setHTMLParserListener(new SilentHTMLParserListener());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getCache().setMaxSize(Integer.MAX_VALUE);
        return webClient;
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void downloadStandardQuality(String URL) {
        WebClient webClient = createWebClient();

        try {
            HtmlPage page = webClient.getPage(URL);

            // -- Step 1: Click on first button --
            HtmlButton dwnButton;
            do {
                sleep();
                dwnButton = page.querySelector("button.btn.btn-success.btn-mp3");
            } while (dwnButton == null);
            page.executeJavaScript("document.querySelector('button.btn.btn-success.btn-mp3').click();");

            // -- Step 2: Click on second button --
            HtmlAnchor aButton;
            do {
                sleep();
                aButton = page.querySelector("a.btn.btn-success.btn-file:not([id])");
            } while (aButton == null);

            WebWindow window = page.getEnclosingWindow();
            page.executeJavaScript("document.querySelectorAll('a.btn.btn-success.btn-file')[1].click();");
            UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();

            // -- Step 3: Download --
            InputStream initialStream = downloadPage.getInputStream();
            File targetFile = new File("src/main/resources/vid_std.mp3");

            java.nio.file.Files.copy(
                    initialStream,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // -- Closing --
            webClient.getCurrentWindow().getJobManager().removeAllJobs();
            webClient.close();

        } catch (IOException e) {
            System.out.println("An error occurred in m_std: " + e);
        }
    }

    public static void downloadBestQuality(String URL) {
        WebClient webClient = createWebClient();

        try {
            HtmlPage page = webClient.getPage(URL);

            // -- Step 0.1: Open quality list --
            HtmlButton dropdownToggle;
            do {
                sleep();
                dropdownToggle = page.querySelector("button.btn.btn-default.dropdown-toggle");
            } while (dropdownToggle == null);
            page.executeJavaScript("document.querySelector('button.btn.btn-default.dropdown-toggle').click()");

            // -- Step 0.2: Choose best quality --
            page.executeJavaScript("document.querySelector('div.col-xs-5.p-t-md > ul > li:nth-child(1) > a').click()");

            // -- Step 1: Click on first button --
            HtmlButton dwnButton;
            do {
                sleep();
                dwnButton = page.querySelector("button.btn.btn-success.btn-mp3");
            } while (dwnButton == null);
            page.executeJavaScript("document.querySelector('button.btn.btn-success.btn-mp3').click();");

            // -- Step 2: Click on second button --
            HtmlAnchor aButton;
            do {
                sleep();
                aButton = page.querySelector("a.btn.btn-success.btn-file:not([id])");
            } while (aButton == null);

            WebWindow window = page.getEnclosingWindow();
            page.executeJavaScript("document.querySelectorAll('a.btn.btn-success.btn-file')[1].click();");
            UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();

            // -- Step 3: Download --
            InputStream initialStream = downloadPage.getInputStream();
            File targetFile = new File("src/main/resources/vid_best.mp3");

            java.nio.file.Files.copy(
                    initialStream,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // -- Closing --
            webClient.getCurrentWindow().getJobManager().removeAllJobs();
            webClient.close();

        } catch (IOException e) {
            System.out.println("An error occurred in m_best: " + e);
        }
    }
}
