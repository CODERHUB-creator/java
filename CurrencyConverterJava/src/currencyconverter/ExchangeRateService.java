package currencyconverter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches live exchange rates without external libraries.
 */
public class ExchangeRateService {
    private static final String API_URL = "https://open.er-api.com/v6/latest/";
    private final HttpClient client;

    public ExchangeRateService() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public RateResult getRates(String baseCurrency) throws IOException, InterruptedException {
        String base = baseCurrency.toUpperCase().trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + base))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API returned HTTP " + response.statusCode());
        }

        String json = response.body();

        if (!json.contains("\"result\":\"success\"")) {
            throw new IOException("Currency API did not return a successful result.");
        }

        String timeLastUpdate = extractString(json, "time_last_update_utc");
        String timeNextUpdate = extractString(json, "time_next_update_utc");

        Map<String, Double> rates = parseRates(json);
        return new RateResult(base, rates, timeLastUpdate, timeNextUpdate);
    }

    private Map<String, Double> parseRates(String json) {
        Map<String, Double> rates = new LinkedHashMap<>();

        int start = json.indexOf("\"rates\":{");
        if (start < 0) {
            return rates;
        }

        start = json.indexOf('{', start) + 1;
        int end = json.indexOf('}', start);
        if (end < 0) {
            return rates;
        }

        String rateSection = json.substring(start, end);
        Pattern p = Pattern.compile("\"([A-Z]{3})\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[Ee][+-]?\\d+)?)");
        Matcher m = p.matcher(rateSection);

        while (m.find()) {
            rates.put(m.group(1), Double.parseDouble(m.group(2)));
        }

        return rates;
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "Unavailable";
    }

    public static class RateResult {
        private final String base;
        private final Map<String, Double> rates;
        private final String lastUpdate;
        private final String nextUpdate;

        public RateResult(String base, Map<String, Double> rates,
                          String lastUpdate, String nextUpdate) {
            this.base = base;
            this.rates = rates;
            this.lastUpdate = lastUpdate;
            this.nextUpdate = nextUpdate;
        }

        public String getBase() {
            return base;
        }

        public Map<String, Double> getRates() {
            return rates;
        }

        public String getLastUpdate() {
            return lastUpdate;
        }

        public String getNextUpdate() {
            return nextUpdate;
        }
    }
}
