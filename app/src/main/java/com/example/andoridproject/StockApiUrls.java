package com.example.andoridproject;

import java.util.Locale;

final class StockApiUrls {
    private static final String SYMBOL_PATTERN = "^[A-Z][A-Z0-9.]{0,9}$";

    private StockApiUrls() {}

    static String finnhubQuoteUrl(String symbol, String token) {
        return "https://finnhub.io/api/v1/quote?symbol="
                + normalizeSymbol(symbol)
                + "&token="
                + requireKey(token, "FINNHUB_TOKEN");
    }

    static String fcsHistoryUrl(String symbol, String apiKey) {
        return "https://fcsapi.com/api-v3/stock/history?symbol="
                + normalizeSymbol(symbol)
                + "&period=1d&access_key="
                + requireKey(apiKey, "FCS_API_KEY");
    }

    private static String normalizeSymbol(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Enter a valid stock symbol.");
        }

        String symbol = value.trim().toUpperCase(Locale.US);
        if (!symbol.matches(SYMBOL_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid stock symbol.");
        }

        return symbol;
    }

    private static String requireKey(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(name + " is missing from local.properties.");
        }

        return value.trim();
    }
}
