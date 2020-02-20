package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.robot.Location;

import java.lang.ref.WeakReference;

import static android.graphics.Paint.Cap.ROUND;


public class RemainingPathView extends SlamwareBaseView {
    private static final String TAG = "RemainingPathView";

    private Paint mPaint;
    private Path mRemainingPath;

    public RemainingPathView(Context context, WeakReference<MapView> parent) {
        super(context, parent);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFF7AAD44);
        mPaint.setStrokeCap(ROUND);
        mPaint.setStrokeWidth(2);
    }

    public void updateRemainingPath(Path remainingPath) {
        this.mRemainingPath = remainingPath;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStrokeWidth(2 * getScale());
        if (mRemainingPath == null || mRemainingPath.getPoints() == null) return;

        for (Location l : mRemainingPath.getPoints()) {
            PointF center = mParent.get().mapCoordinateToWidghtCoordinateF(l.getX(), l.getY());
            canvas.drawPoint(center.x, center.y, mPaint);
        }
    }

}
