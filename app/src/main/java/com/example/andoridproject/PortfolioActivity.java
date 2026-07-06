package com.example.andoridproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioActivity extends AppCompatActivity implements PositionAdapter.OnCloseClickListener {

    private RecyclerView recyclerView;
    private PositionAdapter adapter;
    private List<Position> positionList = new ArrayList<>();
    private Map<Position, String> positionKeys = new HashMap<>();
    private TextView tvBalance;
    private float currentBalance = 0;
    
    private DatabaseReference userRef;
    private ValueEventListener portfolioListener;
    private StockApiClient stockApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        tvBalance = findViewById(R.id.tvPortfolioBalance);
        recyclerView = findViewById(R.id.rvPortfolio);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PositionAdapter(positionList, this);
        recyclerView.setAdapter(adapter);
        
        stockApiClient = new StockApiClient(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadPortfolioData();
    }

    private void loadPortfolioData() {
        portfolioListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("moneyAmount").exists()) {
                    currentBalance = Float.parseFloat(snapshot.child("moneyAmount").getValue().toString());
                    tvBalance.setText("Available Cash: $" + String.format("%.2f", currentBalance));
                }

                positionList.clear();
                positionKeys.clear();
                
                DataSnapshot portfolio = snapshot.child("portfolio");
                for (DataSnapshot ds : portfolio.getChildren()) {
                    Position p = ds.getValue(Position.class);
                    if (p != null) {
                        positionList.add(p);
                        positionKeys.put(p, ds.getKey());
                        
                        // FIX: Fetch live price to calculate correct P/L!
                        fetchLivePriceForPosition(p, ds.getKey()); 
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        userRef.addValueEventListener(portfolioListener);
    }

    private void fetchLivePriceForPosition(Position p, String key) {
        stockApiClient.fetchQuote(p.symbol,
                livePrice -> {
                    // Recalculate P/L with actual live data
                    p.calculatePL(livePrice);
                    adapter.notifyDataSetChanged(); // Update UI with colored P/L text

                    // FEATURE: Check Stop-Loss and Take-Profit
                    boolean shouldClose = false;

                    if ("BUY".equals(p.type)) {
                        if (p.stopLoss > 0 && livePrice <= p.stopLoss) shouldClose = true;
                        if (p.takeProfit > 0 && livePrice >= p.takeProfit) shouldClose = true;
                    } else { // SHORT (Logic is reversed: higher price is bad)
                        if (p.stopLoss > 0 && livePrice >= p.stopLoss) shouldClose = true;
                        if (p.takeProfit > 0 && livePrice <= p.takeProfit) shouldClose = true;
                    }

                    if (shouldClose) {
                        Toast.makeText(this, "Auto-closing " + p.symbol + " (Limit Hit!)", Toast.LENGTH_LONG).show();
                        executeTradeClose(p, key);
                    }
                },
                error -> Log.e("API", "Error: " + error.getMessage()));
    }

    @Override
    public void onCloseClick(Position p) {
        String key = positionKeys.get(p);
        if (key != null) executeTradeClose(p, key);
    }

    private void executeTradeClose(Position p, String key) {
        // Prevent clicking the button twice while it's processing
        if (!positionKeys.containsValue(key)) return; 
        positionKeys.remove(p);

        float cashToReturn = p.getExitValue();
        float newBalance = currentBalance + cashToReturn;

        userRef.child("moneyAmount").setValue(newBalance);

        // FEATURE: Save to Trade History before deleting
        // Make sure currentPrice isn't 0
        p.currentPrice = p.currentPrice > 0 ? p.currentPrice : p.entryPrice; 
        userRef.child("history").push().setValue(p);

        // Remove from active portfolio
        userRef.child("portfolio").child(key).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Position Closed: +$" + String.format("%.2f", cashToReturn), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && portfolioListener != null) {
            userRef.removeEventListener(portfolioListener);
        }
    }
}
