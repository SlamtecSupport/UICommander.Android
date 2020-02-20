package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.slamtec.slamware.geometry.Line;

import java.lang.ref.WeakReference;
import java.util.List;


public class VirtualLineView extends SlamwareBaseView {
    private final static String TAG = VirtualLineView.class.getName();

    private List<Line> mLines;
    private Paint mPaint;
    private int mColor;

    private float LINE_WIDTH = 1;

    WeakReference<MapView> parent;

    public VirtualLineView(Context context, WeakReference<MapView> parent, int color) {
        super(context, parent);
        this.parent = parent;
        mColor = color;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(1);
    }

    public void setLines(List<Line> lines) {
        mLines = lines;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLines == null) return;

        mPaint.setStrokeWidth(LINE_WIDTH * getScale());

        for (Line line : mLines) {
            PointF start = parent.get().mapCoordinateToWidghtCoordinateF(line.getStartPoint());
            PointF end = parent.get().mapCoordinateToWidghtCoordinateF(line.getEndPoint());
            canvas.drawLine(start.x, start.y, end.x, end.y, mPaint);
        }
    }

}