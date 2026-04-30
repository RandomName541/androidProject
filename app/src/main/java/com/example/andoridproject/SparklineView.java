package com.example.andoridproject;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class SparklineView extends View {
    private List<Float> dataPoints = new ArrayList<>();
    private Paint linePaint;
    private Paint fillPaint;
    private Path linePath = new Path();
    private Path fillPath = new Path();

    public SparklineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        // Default green color
        linePaint.setColor(Color.parseColor("#00C853"));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<Float> newData, boolean isPositive) {
        this.dataPoints = newData;
        int color = isPositive ? Color.parseColor("#00C853") : Color.parseColor("#FF5252");
        linePaint.setColor(color);
        fillPaint.setColor(color);
        fillPaint.setAlpha(40); // 40/255 transparency

        // MANDATORY: Tell the view to redraw itself with the new data
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints == null || dataPoints.size() < 2) return;

        float width = getWidth();
        float height = getHeight();
        float max = getMax(dataPoints);
        float min = getMin(dataPoints);
        float range = max - min;

        linePath.reset();
        fillPath.reset();

        float xStep = width / (dataPoints.size() - 1);

        for (int i = 0; i < dataPoints.size(); i++) {
            float x = i * xStep;
            // Normalize the price to fit the view height
            float normalizedY = (dataPoints.get(i) - min) / (range == 0 ? 1 : range);
            float y = height - (normalizedY * height * 0.8f) - (height * 0.1f);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        fillPath.lineTo(width, height);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }

    private float getMax(List<Float> list) {
        float max = -Float.MAX_VALUE;
        for (float f : list) if (f > max) max = f;
        return max;
    }

    private float getMin(List<Float> list) {
        float min = Float.MAX_VALUE;
        for (float f : list) if (f < min) min = f;
        return min;
    }
}