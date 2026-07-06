package com.example.andoridproject;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class StockApiClient {
    public interface SuccessCallback<T> {
        void onSuccess(T value);
    }

    public interface ErrorCallback {
        void onError(Exception error);
    }

    private final RequestQueue requestQueue;

    public StockApiClient(Context context) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void fetchQuote(String symbol, SuccessCallback<Float> onSuccess, ErrorCallback onError) {
        String url;
        try {
            url = StockApiUrls.finnhubQuoteUrl(symbol, BuildConfig.FINNHUB_TOKEN);
        } catch (RuntimeException error) {
            onError.onError(error);
            return;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        onSuccess.onSuccess((float) json.getDouble("c"));
                    } catch (Exception error) {
                        onError.onError(error);
                    }
                },
                onError::onError);
        requestQueue.add(request);
    }

    public void fetchHistory(String symbol, SuccessCallback<List<CandleData>> onSuccess, ErrorCallback onError) {
        String url;
        try {
            url = StockApiUrls.fcsHistoryUrl(symbol, BuildConfig.FCS_API_KEY);
        } catch (RuntimeException error) {
            onError.onError(error);
            return;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getBoolean("status")) {
                            onError.onError(new IllegalStateException(json.optString("msg", "FCS request failed.")));
                            return;
                        }

                        onSuccess.onSuccess(parseFcsHistory(json.get("response")));
                    } catch (Exception error) {
                        onError.onError(error);
                    }
                },
                onError::onError);
        requestQueue.add(request);
    }

    static float parseQuotePrice(Object data) {
        Map<?, ?> quote = asMap(data, "quote");
        return numberValue(quote.get("c"), "c");
    }

    static List<CandleData> parseCandles(Object data) {
        if (!(data instanceof List)) {
            throw new IllegalArgumentException("History response is not a list.");
        }

        List<CandleData> candles = new ArrayList<>();
        for (Object item : (List<?>) data) {
            Map<?, ?> row = asMap(item, "history row");
            candles.add(new CandleData(
                    numberValue(row.get("o"), "o"),
                    numberValue(row.get("c"), "c"),
                    numberValue(row.get("h"), "h"),
                    numberValue(row.get("l"), "l"),
                    stringValue(row.get("date"))
            ));
        }

        return candles;
    }

    static List<Float> parseSparklinePrices(Object data) {
        List<CandleData> candles = parseCandles(data);
        List<Float> prices = new ArrayList<>();
        for (CandleData candle : candles) {
            prices.add(candle.close);
        }

        return prices;
    }

    private static Map<?, ?> asMap(Object data, String name) {
        if (!(data instanceof Map)) {
            throw new IllegalArgumentException(name + " response is not an object.");
        }

        return (Map<?, ?>) data;
    }

    private static float numberValue(Object value, String fieldName) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Market data field " + fieldName + " is not a number.");
        }

        return ((Number) value).floatValue();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private static List<CandleData> parseFcsHistory(Object response) throws Exception {
        if (response instanceof JSONArray) {
            return parseFcsHistoryArray((JSONArray) response);
        }

        if (response instanceof JSONObject) {
            return parseFcsHistoryObject((JSONObject) response);
        }

        throw new IllegalArgumentException("FCS response is missing history data.");
    }

    private static List<CandleData> parseFcsHistoryArray(JSONArray dataArray) throws Exception {
        List<CandleData> list = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            list.add(candleFromJson(dataArray.getJSONObject(i)));
        }

        return list;
    }

    private static List<CandleData> parseFcsHistoryObject(JSONObject dataResponse) throws Exception {
        List<CandleData> list = new ArrayList<>();
        java.util.Iterator<String> keys = dataResponse.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.equals("info") || key.equals("symbol")) continue;
            list.add(0, candleFromJson(dataResponse.getJSONObject(key)));
        }

        return list;
    }

    private static CandleData candleFromJson(JSONObject data) throws Exception {
        String fullDate = data.optString("tm", "");
        String dateLabel = fullDate.length() >= 10 ? fullDate.substring(5, 10) : "";

        return new CandleData(
                (float) data.getDouble("o"),
                (float) data.getDouble("c"),
                (float) data.getDouble("h"),
                (float) data.getDouble("l"),
                dateLabel
        );
    }
}
