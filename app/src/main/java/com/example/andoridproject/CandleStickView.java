package com.example.andoridproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.List;

public class CandleStickView extends View {
    private List<CandleData> data;
    private Paint greenPaint, redPaint, gridPaint, textPaint;

    // Zoom and Pan variables
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleDetector;
    private float scrollOffset = 0f;
    private float lastTouchX;

    public CandleStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Professional Trading Colors
        greenPaint = new Paint();
        greenPaint.setColor(Color.parseColor("#26A69A")); // Emerald Green
        greenPaint.setStyle(Paint.Style.FILL);

        redPaint = new Paint();
        redPaint.setColor(Color.parseColor("#EF5350")); // Coral Red
        redPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1.5f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setData(List<CandleData> data) {
        this.data = data;
        invalidate(); // Redraw view with new data
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (scaleDetector.isInProgress()) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                scrollOffset += dx;
                lastTouchX = event.getX();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        float width = getWidth();
        float height = getHeight() - 80; // Reserve bottom space for X-axis labels
        float chartPaddingRight = 130f; // Space for Y-axis labels

        // 1. Calculate Price Scale (Min/Max)
        float minP = Float.MAX_VALUE;
        float maxP = Float.MIN_VALUE;
        for (CandleData d : data) {
            if (d.low < minP) minP = d.low;
            if (d.high > maxP) maxP = d.high;
        }
        float range = maxP - minP;
        if (range == 0) range = 1; // Prevent division by zero

        // 2. Draw Horizontal Grid & Y-Axis Labels
        for (int i = 0; i <= 5; i++) {
            float price = minP + (range * i / 5);
            float y = height - (i * height / 5);

            // Draw price text aligned to the right
            canvas.drawText(String.format("%.2f", price), width - 65, y + 10, textPaint);
            // Draw horizontal grid line
            canvas.drawLine(0, y, width - chartPaddingRight, y, gridPaint);
        }

        // 3. Draw Candles
        float baseCandleWidth = (width - chartPaddingRight) / 10;
        float candleWidth = baseCandleWidth * scaleFactor;

        for (int i = 0; i < data.size(); i++) {
            CandleData d = data.get(i);
            float x = (i * candleWidth) + scrollOffset;

            // Performance: Only draw candles currently visible on screen
            if (x + candleWidth < 0 || x > width - chartPaddingRight) continue;

            // Map price data to Y coordinates
            float openY = height - ((d.open - minP) / range * height);
            float closeY = height - ((d.close - minP) / range * height);
            float highY = height - ((d.high - minP) / range * height);
            float lowY = height - ((d.low - minP) / range * height);

            Paint p = (d.close >= d.open) ? greenPaint : redPaint;
            float centerX = x + (candleWidth / 2);

            // Draw Wick (Shadow)
            canvas.drawLine(centerX, highY, centerX, lowY, p);

            // Draw Body
            float bodyLeft = x + (candleWidth * 0.15f);
            float bodyRight = x + (candleWidth * 0.85f);
            canvas.drawRect(bodyLeft, Math.min(openY, closeY), bodyRight, Math.max(openY, closeY), p);

            // 4. Draw X-Axis Date Labels (The Debugged Part)
            // Draw a label every 8th candle to avoid overlapping
            if (i % 8 == 0) {
                textPaint.setColor(Color.BLACK);
                String label = (d.date != null) ? d.date : "";

                // Position text at the very bottom of the view
                canvas.drawText(label, centerX, getHeight() - 15, textPaint);

                // Draw a small vertical tick mark
                canvas.drawLine(centerX, height, centerX, height + 10, gridPaint);
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            // Limit zoom range: 0.3x to 5.0x
            scaleFactor = Math.max(0.3f, Math.min(scaleFactor, 5.0f));
            invalidate();
            return true;
        }
    }
}