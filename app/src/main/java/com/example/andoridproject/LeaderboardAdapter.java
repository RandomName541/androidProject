package com.example.andoridproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    // Now using your real User model
    private List<User> userList;
    private String currentUid;

    public LeaderboardAdapter(List<User> userList) {
        this.userList = userList;
        // Get current UID once to use for highlighting the user's row
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Bind data to views
        holder.tvRank.setText("#" + (position + 1));
        holder.tvName.setText(user.getUsername());
        holder.tvBalance.setText("$" + String.format("%.2f", user.getMoneyAmount()));

        // Highlight logic: Check if this row belongs to the logged-in user
        if (currentUid != null && currentUid.equals(user.getUid())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green highlight
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}