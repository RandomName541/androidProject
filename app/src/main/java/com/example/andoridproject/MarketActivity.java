package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MarketActivity extends AppCompatActivity implements StockAdapter.OnStockClickListener {

    private RecyclerView rvMarket;
    private StockAdapter adapter;
    private List<Stock> marketStocks;
    private RequestQueue requestQueue;

    private final String FINNHUB_TOKEN = "d5fo7qhr01qnjhodiehgd5fo7qhr01qnjhodiei0";
    private final String FCS_API_KEY = "sAg1PMIDFvzXBPCiJGOTMS5Atoq5mP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        rvMarket = findViewById(R.id.rvMarket);
        rvMarket.setLayoutManager(new LinearLayoutManager(this));
        requestQueue = Volley.newRequestQueue(this);

        marketStocks = new ArrayList<>();
        String[] tickers = {"AAPL", "TSLA", "MSFT", "GOOGL", "NVDA"};

        for (String t : tickers) marketStocks.add(new Stock(0.0f, t, 0.0f, 0));

        adapter = new StockAdapter(marketStocks, this);
        rvMarket.setAdapter(adapter);

        for (String ticker : tickers) {
            fetchPrice(ticker);
        }
    }

    private void fetchPrice(String ticker) {
        String url = "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + FINNHUB_TOKEN;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        float price = (float) json.getDouble("c");
                        for (int i = 0; i < marketStocks.size(); i++) {
                            if (marketStocks.get(i).getTicker().equals(ticker)) {
                                marketStocks.get(i).setPrice(price);
                                adapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("API", "Error: " + e.getMessage());
                    }
                }, null);
        requestQueue.add(request);
    }

    @Override
    public void onStockClick(Stock stock) {
        if (stock.getPrice() > 0) {
            Intent intent = new Intent(this, ChartActivity.class);
            intent.putExtra("ticker", stock.getTicker());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Loading price...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadSparklineClick(Stock stock, int position) {
        fetchSparklineData(stock.getTicker(), position);
    }

    private void fetchSparklineData(String ticker, int position) {
        String url = "https://fcsapi.com/api-v3/stock/history?symbol=" + ticker + "&period=1d&access_key=" + FCS_API_KEY;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            JSONObject dataResponse = jsonObject.getJSONObject("response");
                            List<Float> history = new ArrayList<>();

                            Iterator<String> keys = dataResponse.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                if (key.equals("info") || key.equals("symbol")) continue;
                                JSONObject d = dataResponse.getJSONObject(key);
                                history.add(0, (float) d.getDouble("c"));
                            }

                            marketStocks.get(position).setHistoricalPrices(history);
                            adapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e("API", "Error: " + e.getMessage());
                    }
                }, null);
        requestQueue.add(request);
    }
}