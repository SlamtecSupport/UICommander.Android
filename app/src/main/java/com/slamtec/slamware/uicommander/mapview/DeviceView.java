package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.uicommander.mapview.utils.RadianUtil;

import java.lang.ref.WeakReference;

import static android.graphics.Paint.Cap.ROUND;

public class DeviceView extends SlamwareBaseView {
    private static final String TAG = "DeviceView";
    public static final int RADIUS_COLOR = 0xC09898FE;

    private float mDeviceRadius = 0.18f;
    private Pose mDevicePose;
    private Paint mPaintRadius;
    private Paint mPaintOrientation;
    private Paint mPaintWhite;

    public DeviceView(Context context, WeakReference<MapView> parent) {
        super(context, parent);
        mDevicePose = new Pose();

        mPaintOrientation = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOrientation.setColor(Color.RED);

        mPaintRadius = new Paint();
        mPaintRadius.setStrokeCap(ROUND);
        mPaintRadius.setColor(RADIUS_COLOR);

        mPaintWhite = new Paint();
        mPaintWhite.setColor(Color.WHITE);
        mPaintWhite.setStrokeWidth(1f);
    }

    public void setDevicePose(Pose pose) {
        if (pose == null) return;
        mDevicePose = pose;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = mDevicePose.getX();
        float y = mDevicePose.getY();
        float yaw = mDevicePose.getYaw();

        PointF center = mParent.get().mapCoordinateToWidghtCoordinateF(x, y);
        float widghtRadius = mParent.get().mapDistanceToWidghtDistance(mDeviceRadius);

        mPaintRadius.setStrokeWidth(widghtRadius * 2f);
        canvas.drawPoint(center.x, center.y, mPaintRadius);

        PointF originalLeft = new PointF(center.x - widghtRadius * 0.25f, center.y - widghtRadius * 0.6f);
        PointF originalRight = new PointF(center.x - widghtRadius * 0.25f, center.y + widghtRadius * 0.6f);
        PointF originalTop = new PointF(center.x + widghtRadius, center.y);
        PointF originalBottom = new PointF(center.x - widghtRadius * 0.5f, center.y);

        Matrix matrix = new Matrix();
        matrix.postRotate(RadianUtil.toAngel(-yaw) + mRotation, center.x, center.y);
        float[] points = new float[]{originalTop.x, originalTop.y, originalLeft.x, originalLeft.y, originalBottom.x, originalBottom.y, originalRight.x, originalRight.y};
        matrix.mapPoints(points);

        Path path = new Path();
        path.moveTo(points[0], points[1]);
        path.lineTo(points[2], points[3]);
        path.lineTo(points[4], points[5]);
        path.lineTo(points[6], points[7]);
        path.lineTo(points[0], points[1]);
        canvas.drawPath(path, mPaintOrientation);

        canvas.drawLine(points[0], points[1], points[4], points[5], mPaintWhite);
        canvas.drawLine(points[2], points[3], center.x, center.y, mPaintWhite);
        canvas.drawLine(points[6], points[7], center.x, center.y, mPaintWhite);
    }

}
