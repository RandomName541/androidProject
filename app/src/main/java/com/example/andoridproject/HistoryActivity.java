package com.example.andoridproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PositionAdapter adapter;
    private List<Position> positionList = new ArrayList<>();
    
    private DatabaseReference historyRef;
    private ValueEventListener historyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass TRUE to hide the 'Close' button
        adapter = new PositionAdapter(positionList, null, true);
        recyclerView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("history");

        loadHistoryData();
    }

    private void loadHistoryData() {
        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                positionList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Position p = ds.getValue(Position.class);
                    if (p != null) positionList.add(p);
                }
                // Reverse it so the most recent trades show at the top
                Collections.reverse(positionList);
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        historyRef.addValueEventListener(historyListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyRef != null && historyListener != null) {
            historyRef.removeEventListener(historyListener);
        }
    }
}
