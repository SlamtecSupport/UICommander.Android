package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.robot.Location;

import java.lang.ref.WeakReference;

import static android.graphics.Paint.Cap.ROUND;

public class RemainingMilestonesView extends SlamwareBaseView {
    private static final String TAG = "RemainingMilestonesView";

    private Path mRemainingMilestones;
    private Paint mPaint;


    public RemainingMilestonesView(Context context, WeakReference<MapView> parent) {
        super(context, parent);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeCap(ROUND);
        mPaint.setStrokeWidth(2);
    }

    public void updateRemainingMilestones(Path remainingMilestones) {
        this.mRemainingMilestones = remainingMilestones;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStrokeWidth(2 * getScale());
        if (mRemainingMilestones == null || mRemainingMilestones.getPoints() == null) return;

        for (Location l : mRemainingMilestones.getPoints()) {
            PointF center = mParent.get().mapCoordinateToWidghtCoordinateF(l.getX(), l.getY());
            Rect rect = new Rect((int) (center.x - 2 * getScale()), (int) (center.y - 2 * getScale()), (int) (center.x + 2 * getScale()), (int) (center.y + 2 * getScale()));
            canvas.drawRect(rect, mPaint);
        }
    }

}
