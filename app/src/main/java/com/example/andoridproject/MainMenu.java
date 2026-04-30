package com.example.andoridproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MainMenu extends AppCompatActivity {

    private TextView tvBalance;
    private DatabaseReference userRef;
    private ValueEventListener balanceListener; // Added for memory leak fix

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        tvBalance = findViewById(R.id.tvQuickBalance);
        Button btnMarket = findViewById(R.id.btnMarket);
        Button btnPortfolio = findViewById(R.id.btnPortfolio);
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        Button btnHistory = findViewById(R.id.btnHistory);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            setupBalanceListener();
        }

        btnMarket.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, MarketActivity.class)));
        btnPortfolio.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, PortfolioActivity.class)));
        btnLeaderboard.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, LeaderboardActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, HistoryActivity.class)));

    }

    private void setupBalanceListener() {
        balanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float balance = Float.parseFloat(snapshot.getValue().toString());
                    tvBalance.setText("Current Balance: $" + String.format("%.2f", balance));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userRef.child("moneyAmount").addValueEventListener(balanceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && balanceListener != null) {
            userRef.child("moneyAmount").removeEventListener(balanceListener);
        }
    }
}
