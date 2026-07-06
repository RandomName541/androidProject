package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity implements StockAdapter.OnStockClickListener {

    private RecyclerView rvMarket;
    private StockAdapter adapter;
    private List<Stock> marketStocks;
    private StockApiClient stockApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        rvMarket = findViewById(R.id.rvMarket);
        rvMarket.setLayoutManager(new LinearLayoutManager(this));
        stockApiClient = new StockApiClient(this);

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
        stockApiClient.fetchQuote(ticker,
                price -> {
                    for (int i = 0; i < marketStocks.size(); i++) {
                        if (marketStocks.get(i).getTicker().equals(ticker)) {
                            marketStocks.get(i).setPrice(price);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> Log.e("API", "Error: " + error.getMessage()));
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
        stockApiClient.fetchHistory(ticker,
                candles -> {
                    List<Float> history = new ArrayList<>();
                    for (CandleData candle : candles) {
                        history.add(candle.close);
                    }

                    marketStocks.get(position).setHistoricalPrices(history);
                    adapter.notifyItemChanged(position);
                },
                error -> Log.e("API", "Error: " + error.getMessage()));
    }
}
