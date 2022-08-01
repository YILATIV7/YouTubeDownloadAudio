import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class AudioDownloader {

    private static final String PRIMARY_DOWNLOAD_BUTTON_SELECTOR = "button.btn.btn-success.btn-mp3";
    private static final String SECOND_DOWNLOAD_BUTTON_SELECTOR = "a.btn.btn-success.btn-file:not([id])";
    private static final String DROPDOWN_TOGGLE_SELECTOR = "button.btn.btn-default.dropdown-toggle";
    private static final String BEST_QUALITY_OPTION_SELECTOR = "div.col-xs-5.p-t-md > ul > li:first-child > a";
    private static final String MIN_QUALITY_OPTION_SELECTOR = "div.col-xs-5.p-t-md > ul > li:last-child > a";

    static {
        // Disabling HTMLUnit logger
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
    }

    private static WebClient createWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setIncorrectnessListener((message, origin) -> {
        });
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

    private static DomElement waitForElementBySelector(HtmlPage page, String selector) {
        DomElement el;
        do {
            sleep();
            el = page.querySelector(selector);
        } while (el == null);
        return el;
    }

    private static void clickOn(HtmlPage page, String elementSelector) {
        page.executeJavaScript(String.format("document.querySelector('%s').click();", elementSelector));
    }

    private static void chooseQuality(HtmlPage page, Quality quality) {
        if (quality == Quality.BEST) {
            // -- Step 0.1: Open quality list --
            waitForElementBySelector(page, DROPDOWN_TOGGLE_SELECTOR);
            clickOn(page, DROPDOWN_TOGGLE_SELECTOR);

            // -- Step 0.2: Choose best quality --
            clickOn(page, BEST_QUALITY_OPTION_SELECTOR);
        } else if (quality == Quality.MIN) {
            // -- Step 0.1: Open quality list --
            waitForElementBySelector(page, DROPDOWN_TOGGLE_SELECTOR);
            clickOn(page, DROPDOWN_TOGGLE_SELECTOR);

            // -- Step 0.2: Choose best quality --
            clickOn(page, MIN_QUALITY_OPTION_SELECTOR);
        } else {
            // do nothing...
        }
    }

    public static void download(String URL, String savePath, Quality quality) throws IOException {
        WebClient webClient = createWebClient();

        HtmlPage page = webClient.getPage(URL);

        chooseQuality(page, quality);

        // -- Step 1: Click on first button --
        waitForElementBySelector(page, PRIMARY_DOWNLOAD_BUTTON_SELECTOR);
        clickOn(page, PRIMARY_DOWNLOAD_BUTTON_SELECTOR);

        // -- Step 2: Click on second button --
        waitForElementBySelector(page, SECOND_DOWNLOAD_BUTTON_SELECTOR);
        WebWindow window = page.getEnclosingWindow();
        clickOn(page, SECOND_DOWNLOAD_BUTTON_SELECTOR);
        UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();

        // -- Step 3: Download --
        InputStream inputStream = downloadPage.getInputStream();
        File targetFile = new File(savePath);
        Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // -- Closing --
        webClient.getCurrentWindow().getJobManager().removeAllJobs();
        webClient.close();
    }
}
