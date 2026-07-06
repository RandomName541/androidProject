package com.example.andoridproject;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StockApiClientTest {
    @Test
    public void parseQuotePriceReadsCurrentPrice() {
        Map<String, Object> quote = new HashMap<>();
        quote.put("c", 192.45);

        assertEquals(192.45f, StockApiClient.parseQuotePrice(quote), 0.001f);
    }

    @Test
    public void parseCandlesReadsHistoryRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("o", 10.0);
        row.put("c", 12.5);
        row.put("h", 13.0);
        row.put("l", 9.5);
        row.put("date", "07-06");
        rows.add(row);

        List<CandleData> candles = StockApiClient.parseCandles(rows);

        assertEquals(1, candles.size());
        assertEquals(10.0f, candles.get(0).open, 0.001f);
        assertEquals(12.5f, candles.get(0).close, 0.001f);
        assertEquals(13.0f, candles.get(0).high, 0.001f);
        assertEquals(9.5f, candles.get(0).low, 0.001f);
        assertEquals("07-06", candles.get(0).date);
    }

    @Test
    public void parseSparklinePricesUsesClosePricesInOrder() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(historyRow(101.0));
        rows.add(historyRow(103.5));

        List<Float> prices = StockApiClient.parseSparklinePrices(rows);

        assertEquals(2, prices.size());
        assertEquals(101.0f, prices.get(0), 0.001f);
        assertEquals(103.5f, prices.get(1), 0.001f);
    }

    private Map<String, Object> historyRow(double close) {
        Map<String, Object> row = new HashMap<>();
        row.put("o", close - 1);
        row.put("c", close);
        row.put("h", close + 1);
        row.put("l", close - 2);
        row.put("date", "07-06");
        return row;
    }
}
