package com.example.andoridproject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class StockApiUrlsTest {
    @Test
    public void quoteUrlUsesFinnhubTokenAndUppercaseSymbol() {
        String url = StockApiUrls.finnhubQuoteUrl(" aapl ", "secret-token");

        assertEquals("https://finnhub.io/api/v1/quote?symbol=AAPL&token=secret-token", url);
    }

    @Test
    public void historyUrlUsesFcsKeyAndUppercaseSymbol() {
        String url = StockApiUrls.fcsHistoryUrl(" tsla ", "secret-key");

        assertEquals("https://fcsapi.com/api-v3/stock/history?symbol=TSLA&period=1d&access_key=secret-key", url);
    }

    @Test
    public void urlBuilderRejectsBlankSymbol() {
        assertThrows(IllegalArgumentException.class, () -> StockApiUrls.finnhubQuoteUrl(" ", "secret-token"));
    }

    @Test
    public void urlBuilderRejectsBlankApiKey() {
        assertThrows(IllegalStateException.class, () -> StockApiUrls.fcsHistoryUrl("AAPL", ""));
    }
}
