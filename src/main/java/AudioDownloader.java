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

    private final HtmlPage page;

    public AudioDownloader(String URL, String savePath, Quality quality, Logger logger) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setIncorrectnessListener((message, origin) -> {
        });
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setHTMLParserListener(new SilentHTMLParserListener());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getCache().setMaxSize(Integer.MAX_VALUE);
        logger.log("-- WebClient constructed");

        //
        page = webClient.getPage(URL);
        logger.log("-- Page received");

        chooseQuality(quality);
        logger.log("-- Quality chose");

        // -- Step 1: Click on first button --
        waitForElementBySelector(PRIMARY_DOWNLOAD_BUTTON_SELECTOR);
        clickOn(PRIMARY_DOWNLOAD_BUTTON_SELECTOR);
        logger.log("-- Clicked on first button");

        // -- Step 2: Click on second button --
        waitForElementBySelector(SECOND_DOWNLOAD_BUTTON_SELECTOR);
        WebWindow window = page.getEnclosingWindow();
        clickOn(SECOND_DOWNLOAD_BUTTON_SELECTOR);
        UnexpectedPage downloadPage = (UnexpectedPage) window.getEnclosedPage();
        logger.log("-- Clicked on second button");

        // -- Step 3: Download --
        logger.log("-- File downloading started");
        InputStream inputStream = downloadPage.getInputStream();
        File targetFile = new File(savePath);
        Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        logger.log("-- File downloaded");

        // -- Closing --
        logger.log("-- Closing WebClient");
        webClient.getCurrentWindow().getJobManager().removeAllJobs();
        webClient.close();
        logger.log("-- Closed");
    }

    private void chooseQuality(Quality quality) {
        if (quality == Quality.BEST) {
            // -- Step 0.1: Open quality list --
            waitForElementBySelector(DROPDOWN_TOGGLE_SELECTOR);
            clickOn(DROPDOWN_TOGGLE_SELECTOR);

            // -- Step 0.2: Choose best quality --
            clickOn(BEST_QUALITY_OPTION_SELECTOR);
        } else if (quality == Quality.MIN) {
            // -- Step 0.1: Open quality list --
            waitForElementBySelector(DROPDOWN_TOGGLE_SELECTOR);
            clickOn(DROPDOWN_TOGGLE_SELECTOR);

            // -- Step 0.2: Choose best quality --
            clickOn(MIN_QUALITY_OPTION_SELECTOR);
        } else {
            // do nothing...
        }
    }

    private void waitForElementBySelector(String selector) {
        DomElement el;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            el = page.querySelector(selector);
        } while (el == null);
    }

    private void clickOn(String elementSelector) {
        page.executeJavaScript(String.format("document.querySelector('%s').click();", elementSelector));
    }
}
