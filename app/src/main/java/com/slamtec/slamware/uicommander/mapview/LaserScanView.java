package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.slamtec.slamware.robot.LaserPoint;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Pose;

import java.lang.ref.WeakReference;
import java.util.Vector;

import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;

public class LaserScanView extends SlamwareBaseView {

    private LaserScan mLaserScan;
    private Paint mPaintLaserPoint;
    private Paint mPaintLaserArea;
    private Paint mPaintLaserAreaEdge;

    public LaserScanView(Context context, WeakReference<MapView> parent) {
        super(context, parent);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mParent = parent;

        mPaintLaserPoint = new Paint();
        mPaintLaserPoint.setColor(Color.RED);
        mPaintLaserPoint.setStrokeWidth(4);
        mPaintLaserPoint.setStyle(STROKE);

        mPaintLaserArea = new Paint();
        mPaintLaserArea.setARGB(50, 150, 0, 0);
        mPaintLaserArea.setStrokeWidth(1);
        mPaintLaserArea.setStyle(FILL);

        mPaintLaserAreaEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLaserAreaEdge.setARGB(190, 255, 0, 0);
        mPaintLaserAreaEdge.setStrokeWidth(0.3f);
        mPaintLaserAreaEdge.setStyle(STROKE);
    }

    public void updateLaserScan(LaserScan laserScan) {
        if (laserScan == null) return;

        mLaserScan = laserScan;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLaserScan == null) return;

        PointF uiCenter;
        PointF centerPosition = new PointF();
        Path pathLaserArea = new Path();

        Pose robotPose = mLaserScan.getPose();
        PointF uiRobotPonit = mParent.get().mapCoordinateToWidghtCoordinateF(robotPose.getX(), robotPose.getY());

        pathLaserArea.moveTo(uiRobotPonit.x, uiRobotPonit.y);

        Vector<LaserPoint> scanPoints = mLaserScan.getLaserPoints();

        for (LaserPoint scanPoint : scanPoints) {
            if (!scanPoint.isValid()) continue;

            double phi = scanPoint.getAngle() + robotPose.getYaw();
            double r = scanPoint.getDistance();

            double physicalX = robotPose.getX() + r * Math.cos(phi);
            double physicalY = robotPose.getY() + r * Math.sin(phi);

            centerPosition.x = ((float) physicalX);
            centerPosition.y = ((float) physicalY);

            uiCenter = mParent.get().mapCoordinateToWidghtCoordinateF(centerPosition.x, centerPosition.y);

            canvas.drawPoint(uiCenter.x, uiCenter.y, mPaintLaserPoint);

            pathLaserArea.lineTo(uiCenter.x, uiCenter.y);
        }

        pathLaserArea.lineTo(uiRobotPonit.x, uiRobotPonit.y);
        pathLaserArea.close();

        canvas.drawPath(pathLaserArea, mPaintLaserArea);
        canvas.drawPath(pathLaserArea, mPaintLaserAreaEdge);
    }
}
