import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;

import java.net.URL;

public class SilentHTMLParserListener implements HTMLParserListener {
    @Override
    public void error(String message, URL url, String html, int line, int column, String key) {
        // silence
    }

    @Override
    public void warning(String message, URL url, String html, int line, int column, String key) {
        // silence
    }
}
