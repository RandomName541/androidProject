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
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private CandleStickView myCustomChart;
    private String symbol;
    private float currentPrice = 0f;
    private float userBalance = 0f;
    private TextView tvTitle;
    private DatabaseReference userRef;
    
    private ValueEventListener balanceListener; // Memory leak fix
    private final String FCS_API_KEY = "sAg1PMIDFvzXBPCiJGOTMS5Atoq5mP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        symbol = getIntent().getStringExtra("ticker");
        tvTitle = findViewById(R.id.tvChartTitle);
        tvTitle.setText(symbol + " Chart");
        myCustomChart = findViewById(R.id.myCustomChart);

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
        String url = "https://fcsapi.com/api-v3/stock/history?symbol=" + symbol + "&period=1d&access_key=" + FCS_API_KEY;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            // FIX: FCS returns a JSON Array, not a JSON Object
                            org.json.JSONArray dataArray = jsonObject.getJSONArray("response");
                            List<CandleData> list = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject d = dataArray.getJSONObject(i);
                                currentPrice = (float) d.getDouble("c"); // Updates to the latest price

                                // Format the date (FCS returns "YYYY-MM-DD HH:MM:SS" in the "tm" field)
                                String fullDate = d.optString("tm", "");
                                String dateLabel = fullDate.length() >= 10 ? fullDate.substring(5, 10) : "";

                                list.add(new CandleData(
                                        (float) d.getDouble("o"),
                                        (float) d.getDouble("c"),
                                        (float) d.getDouble("h"),
                                        (float) d.getDouble("l"),
                                        dateLabel
                                ));
                            }
                            myCustomChart.setData(list);
                        } else {
                            // If API limit is reached or key is wrong, show a message
                            Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("ChartError", "Parsing Error: " + e.getMessage());
                    }
                }, error -> Log.e("ChartError", "Volley Error: " + error.toString()));
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && balanceListener != null) {
            userRef.child("moneyAmount").removeEventListener(balanceListener);
        }
    }
}
