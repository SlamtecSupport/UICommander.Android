package com.slamtec.slamware.uicommander.mapview.utils;

import android.graphics.Point;
import android.graphics.PointF;

import com.slamtec.slamware.geometry.Size;

final public class PointTranslateUtil {

    public static PointF translate(com.slamtec.slamware.geometry.PointF pt) {
        return new PointF(pt.getX(), pt.getY());
    }

    public static com.slamtec.slamware.geometry.PointF translate(PointF pt) {
        return new com.slamtec.slamware.geometry.PointF(pt.x, pt.y);
    }

    public static Point translate(Size sz) {
        return new Point(sz.getWidth(), sz.getHeight());
    }

}
