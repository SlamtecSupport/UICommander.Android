package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.slamtec.slamware.robot.Pose;

import java.lang.ref.WeakReference;

public class HomeDockView extends SlamwareBaseView {
    private static final String TAG = "HomeDockView";

    private Pose mHomePose;
    private Paint mPaint;

    public HomeDockView(Context context, WeakReference<MapView> parent) {
        super(context, parent);
        mHomePose = null;

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(1f);
    }

    public void setHomePose(Pose pose) {
        mHomePose = pose;
        if (mHomePose != null) invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mHomePose == null) return;

        float x = mHomePose.getX();
        float y = mHomePose.getY();
        float yaw = mHomePose.getYaw();

        PointF center = mParent.get().mapCoordinateToWidghtCoordinateF(x, y);
        canvas.drawCircle(center.x, center.y, getScale() * 3, mPaint);
    }

}
