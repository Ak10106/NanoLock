package jp.ac.titech.itpro.sdl.suggest;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Suggester {

    private final String baseUrl;

    public Suggester(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> suggest(String query) {
        try {
            HttpURLConnection conn = getConnection(getURL(query));
            try {
                InputStream in = conn.getInputStream();
                return parse(in, "UTF-8");
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            List<String> result = new ArrayList<>();
            result.add(e.toString());
            return result;
        }
    }

    private URL getURL(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        return new URL(baseUrl + encodedQuery);
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection result = (HttpURLConnection) url.openConnection();
        result.setConnectTimeout(10000);
        result.setDoInput(true);
        result.connect();
        return result;
    }

    private List<String> parse(InputStream input, String encoding) throws XmlPullParserException, IOException {
        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(input, encoding);

        List<String> result = new ArrayList<>();
        for (int et = xpp.getEventType(); et != XmlPullParser.END_DOCUMENT; et = xpp.next()) {
            if (et == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("suggestion")) {
                for (int i = 0; i < xpp.getAttributeCount(); i++) {
                    if (xpp.getAttributeName(i).equalsIgnoreCase("data")) {
                        result.add(xpp.getAttributeValue(i));
                    }
                }
            }
        }
        return result;
    }
}
