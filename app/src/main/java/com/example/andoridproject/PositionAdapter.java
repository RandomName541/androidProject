package com.example.andoridproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {
    private List<Position> positions;
    private OnCloseClickListener listener;
    private boolean isHistory; // NEW: Flag to determine if this is a historical trade

    public PositionAdapter(List<Position> positionList, PortfolioActivity listener) {
        this.positions = positionList;
        this.listener = listener;
    }

    public interface OnCloseClickListener {
        void onCloseClick(Position position);
    }

    // NEW: Pass 'true' for isHistory when creating this in HistoryActivity
    public PositionAdapter(List<Position> positions, OnCloseClickListener listener, boolean isHistory) {
        this.positions = positions;
        this.listener = listener;
        this.isHistory = isHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_position, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Position p = positions.get(position);

        holder.tvSymbol.setText(p.symbol + " (" + p.type + ")");
        holder.tvQty.setText("Shares: " + p.qty);
        holder.tvEntry.setText("Entry: $" + String.format("%.2f", p.entryPrice));
        holder.tvPL.setText("P/L: $" + String.format("%.2f", p.pl));
        holder.tvPL.setTextColor(p.pl >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        // If it's a history item, hide the Close button and change the limits text
        if (isHistory) {
            holder.btnClose.setVisibility(View.GONE);
            holder.tvLimits.setText("Closed at $" + String.format("%.2f", p.currentPrice));
        } else {
            holder.btnClose.setVisibility(View.VISIBLE);
            String limits = "SL: " + (p.stopLoss > 0 ? p.stopLoss : "None") +
                    " | TP: " + (p.takeProfit > 0 ? p.takeProfit : "None");
            holder.tvLimits.setText(limits);
        }

        holder.btnClose.setOnClickListener(v -> {
            if (listener != null) listener.onCloseClick(p);
        });
    }

    @Override
    public int getItemCount() { return positions.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvQty, tvEntry, tvPL, tvLimits;
        Button btnClose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tvPosSymbol);
            tvQty = itemView.findViewById(R.id.tvPosQty);
            tvEntry = itemView.findViewById(R.id.tvPosEntry);
            tvPL = itemView.findViewById(R.id.tvPosPL);
            tvLimits = itemView.findViewById(R.id.tvPosLimits);
            btnClose = itemView.findViewById(R.id.btnPosClose);
        }
    }
}
