package com.example.andoridproject;

public class CandleData {
    public float open, close, high, low;
    public String date;

    public CandleData(float open, float close, float high, float low, String date) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.date = date;
    }
}