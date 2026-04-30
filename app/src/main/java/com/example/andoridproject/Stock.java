package com.example.andoridproject;

import java.io.Serializable;
import java.util.List; // Added import

public class Stock implements Serializable {

    private float price;
    private String ticker;
    private float dailyChangePercent;
    private int quantity;
    private List<Float> historicalPrices; // Added field for sparkline data

    public Stock() {}

    public Stock(float price, String ticker, float dailyChangePercent, int quantity) {
        this.price = price;
        this.ticker = ticker;
        this.dailyChangePercent = dailyChangePercent;
        this.quantity = quantity;
    }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public float getDailyChangePercent() { return dailyChangePercent; }
    public void setDailyChangePercent(float dailyChangePercent) { this.dailyChangePercent = dailyChangePercent; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Added Getter and Setter
    public List<Float> getHistoricalPrices() { return historicalPrices; }
    public void setHistoricalPrices(List<Float> historicalPrices) { this.historicalPrices = historicalPrices; }
}