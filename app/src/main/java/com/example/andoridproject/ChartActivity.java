package com.example.andoridproject;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private CandleStickView myCustomChart;
    private String symbol;
    private float currentPrice = 0f;
    private float userBalance = 0f;
    private TextView tvTitle;
    private DatabaseReference userRef;
    private StockApiClient stockApiClient;
    
    private ValueEventListener balanceListener; // Memory leak fix

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        symbol = getIntent().getStringExtra("ticker");
        tvTitle = findViewById(R.id.tvChartTitle);
        tvTitle.setText(symbol + " Chart");
        myCustomChart = findViewById(R.id.myCustomChart);
        stockApiClient = new StockApiClient(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        balanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) userBalance = Float.parseFloat(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        userRef.child("moneyAmount").addValueEventListener(balanceListener);

        findViewById(R.id.btnBuyStock).setOnClickListener(v -> showTradeDialog(true));
        findViewById(R.id.btnShortStock).setOnClickListener(v -> showTradeDialog(false));

        fetchChartData();
    }

    private void showTradeDialog(boolean isBuy) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(isBuy ? "Buy " + symbol : "Short " + symbol);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText qtyInput = new EditText(this);
        qtyInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        qtyInput.setHint("Quantity");
        layout.addView(qtyInput);

        // FEATURE: STOP LOSS / TAKE PROFIT INPUTS
        final EditText slInput = new EditText(this);
        slInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        slInput.setHint("Stop-Loss Price (Optional)");
        layout.addView(slInput);

        final EditText tpInput = new EditText(this);
        tpInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tpInput.setHint("Take-Profit Price (Optional)");
        layout.addView(tpInput);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String qtyStr = qtyInput.getText().toString();
            String slStr = slInput.getText().toString();
            String tpStr = tpInput.getText().toString();

            if (!qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                float sl = slStr.isEmpty() ? 0f : Float.parseFloat(slStr);
                float tp = tpStr.isEmpty() ? 0f : Float.parseFloat(tpStr);
                handleTrade(isBuy, qty, sl, tp);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void handleTrade(boolean isBuy, int quantity, float stopLoss, float takeProfit) {
        if (currentPrice <= 0) {
            Toast.makeText(this, "Price not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        float cost = quantity * currentPrice;

        if (userBalance >= cost) {
            float newBalance = userBalance - cost;
            userRef.child("moneyAmount").setValue(newBalance);

            DatabaseReference portfolioRef = userRef.child("portfolio").push();
            Position newPos = new Position(symbol, isBuy ? "BUY" : "SHORT", currentPrice, quantity);
            
            // Assign limits
            newPos.stopLoss = stopLoss;
            newPos.takeProfit = takeProfit;

            portfolioRef.setValue(newPos).addOnSuccessListener(aVoid -> {
                Toast.makeText(ChartActivity.this, "Trade Executed!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Insufficient Funds!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchChartData() {
        stockApiClient.fetchHistory(symbol,
                list -> {
                    if (!list.isEmpty()) {
                        currentPrice = list.get(list.size() - 1).close;
                    }
                    myCustomChart.setData(list);
                },
                error -> {
                    Log.e("ChartError", "Functions Error: " + error.getMessage());
                    Toast.makeText(this, "Unable to load chart data", Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && balanceListener != null) {
            userRef.child("moneyAmount").removeEventListener(balanceListener);
        }
    }
}
