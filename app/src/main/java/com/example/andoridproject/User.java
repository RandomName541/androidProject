package com.example.andoridproject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String uid;
    private float moneyAmount;
    private String username;
    private List<Stock> stocks;

    // 1. Required empty constructor for Firebase
    public User() {
        this.stocks = new ArrayList<>(); // Initialize to prevent NullPointer errors
    }

    // 2. Full constructor for Signup/Leaderboard use
    public User(String uid, float moneyAmount, String username, List<Stock> stocks) {
        this.uid = uid;
        this.moneyAmount = moneyAmount;
        this.username = username;
        this.stocks = (stocks != null) ? stocks : new ArrayList<>();
    }

    // 3. Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public float getMoneyAmount() { return moneyAmount; }
    public void setMoneyAmount(float moneyAmount) { this.moneyAmount = moneyAmount; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<Stock> getStocks() { return stocks; }
    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }
}