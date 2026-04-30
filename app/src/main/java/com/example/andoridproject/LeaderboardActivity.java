package com.example.andoridproject;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private List<User> topPlayers = new ArrayList<>();
    private LinearLayout userRankHeader;
    private TextView tvMyRank, tvMyName, tvMyBalance;
    private DatabaseReference mDatabase;
    
    // Memory leak fix: Store query and listener
    private Query topPlayersQuery;
    private ValueEventListener leaderboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        userRankHeader = findViewById(R.id.userRankHeader);
        tvMyRank = findViewById(R.id.tvMyRank);
        tvMyName = findViewById(R.id.tvMyName);
        tvMyBalance = findViewById(R.id.tvMyBalance);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fetchTopPlayers();
    }

    private void fetchTopPlayers() {
        // SCALABILITY FIX: Let Firebase sort and limit the data to Top 10
        topPlayersQuery = mDatabase.orderByChild("moneyAmount").limitToLast(10);
        
        leaderboardListener = topPlayersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topPlayers.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if (user != null) topPlayers.add(user);
                }
                // Firebase returns data ascending (lowest to highest), so we reverse it
                Collections.reverse(topPlayers); 
                updateUI();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUI() {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRankHeader.setVisibility(View.GONE);

        // Check if current user is in the Top 10
        for (int i = 0; i < topPlayers.size(); i++) {
            if (topPlayers.get(i).getUid() != null && topPlayers.get(i).getUid().equals(myUid)) {
                userRankHeader.setVisibility(View.VISIBLE);
                tvMyRank.setText("#" + (i + 1));
                tvMyName.setText(topPlayers.get(i).getUsername());
                tvMyBalance.setText("$" + String.format("%.2f", topPlayers.get(i).getMoneyAmount()));
                break;
            }
        }
        rvLeaderboard.setAdapter(new LeaderboardAdapter(topPlayers));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // MEMORY LEAK FIX: Remove listener when activity is destroyed
        if (topPlayersQuery != null && leaderboardListener != null) {
            topPlayersQuery.removeEventListener(leaderboardListener);
        }
    }
}
