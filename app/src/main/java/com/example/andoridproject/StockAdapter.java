package com.example.andoridproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<Stock> stockList;
    private OnStockClickListener listener;

    public interface OnStockClickListener {
        void onStockClick(Stock stock);
        void onLoadSparklineClick(Stock stock, int position); // Added to interface
    }

    public StockAdapter(List<Stock> stockList, OnStockClickListener listener) {
        this.stockList = stockList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.tvTicker.setText(stock.getTicker());
        holder.tvCompanyName.setText(stock.getTicker() + " Corp.");
        holder.tvPrice.setText("$" + String.format("%.2f", stock.getPrice()));

        // Logic to show chart if data exists, otherwise show the button
        if (stock.getHistoricalPrices() != null && !stock.getHistoricalPrices().isEmpty()) {
            holder.btnLoadSparkline.setVisibility(View.GONE);
            holder.sparklineView.setVisibility(View.VISIBLE);

            boolean isPositive = stock.getHistoricalPrices().get(stock.getHistoricalPrices().size()-1) >= stock.getHistoricalPrices().get(0);
            holder.sparklineView.setData(stock.getHistoricalPrices(), isPositive);
        } else {
            holder.btnLoadSparkline.setVisibility(View.VISIBLE);
            holder.sparklineView.setVisibility(View.GONE);

            holder.btnLoadSparkline.setOnClickListener(v -> {
                if (listener != null) listener.onLoadSparklineClick(stock, position);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStockClick(stock);
        });
    }

    @Override
    public int getItemCount() { return stockList.size(); }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicker, tvPrice, tvCompanyName;
        Button btnLoadSparkline;
        SparklineView sparklineView;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            btnLoadSparkline = itemView.findViewById(R.id.btnLoadSparkline);
            sparklineView = itemView.findViewById(R.id.sparklineView);
        }
    }
}