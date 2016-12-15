package anaphora.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unbescape.html.HtmlEscape;

public class GoogleHelper {
    private static final String GOOGLE_SEARCH_URL = "http://www.google.com/search?q=";
    private static final int GOOGLE_SEARCH_TIMEOUT = 7000;

    private static Map<String, Integer> cache = new HashMap<>();

    public static int occurNumber(String noun, String indicator) throws IOException, InterruptedException {
        String key = "\"" + noun + " " + indicator + "\"";
        Integer cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Thread.sleep(GOOGLE_SEARCH_TIMEOUT);

        String input = queryForResponse(key), number = "";
        Pattern pattern = Pattern.compile("(<div class=\"sd\" id=\"resultStats\">)(.*)(</div>)<div id=\"res\">");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] a = matcher.group(0).split(" ");
            number = a[a.length - 2].substring(0, a[a.length - 2].length() - 10);
            number = HtmlEscape.unescapeHtml(number).replaceAll(" ", "");
        }

        int result = new BigInteger(number).intValue();
        cache.put(key, result);
        return result;
    }

    private static String queryForResponse(String key) throws IOException {
        String query = String.format("%s", URLEncoder.encode(key, "UTF-8"));
        URLConnection con = new URL(GOOGLE_SEARCH_URL + query).openConnection();
        con.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        String response = "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
        }

        return response;
    }
}
