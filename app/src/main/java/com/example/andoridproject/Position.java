package com.example.andoridproject;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Position {
    public String symbol;
    public String type; // "BUY" or "SHORT"
    public float entryPrice;
    public float currentPrice;
    public float pl;
    public int qty;
    public float stopLoss;
    public float takeProfit;

    // Required for Firebase mapping
    public Position() {}

    public Position(String symbol, String type, float entryPrice, int qty) {
        this.symbol = symbol;
        this.type = type;
        this.entryPrice = entryPrice;
        this.qty = qty;
        this.stopLoss = 0f;
        this.takeProfit = 0f;
    }

    public void calculatePL(float livePrice) {
        this.currentPrice = livePrice;
        if ("BUY".equals(this.type)) {
            this.pl = (livePrice - entryPrice) * qty;
        } else {
            this.pl = (entryPrice - livePrice) * qty;
        }
    }

    // This calculates exactly how much cash is returned to the user
    public float getExitValue() {
        if ("BUY".equals(type)) {
            // Selling shares: you get current price * quantity
            return currentPrice * qty;
        } else {
            // Covering short: you get your initial collateral + profit (or - loss)
            return (entryPrice * qty) + pl;
        }
    }
}