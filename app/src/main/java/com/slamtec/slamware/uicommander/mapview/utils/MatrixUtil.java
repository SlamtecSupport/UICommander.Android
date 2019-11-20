package com.slamtec.slamware.uicommander.mapview.utils;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.renderscript.Matrix2f;

public class MatrixUtil {

    public static void calculateRectTranslateMatrix(RectF from, RectF to, Matrix result) {
        if (from == null || to == null || result == null) {
            return;
        }
        if (from.width() == 0 || from.height() == 0) {
            return;
        }
        result.reset();
        result.postTranslate(-from.left, -from.top);
        result.postScale(to.width() / from.width(), to.height() / from.height());
        result.postTranslate(to.left, to.top);
    }

    public static Matrix2f multiply(Matrix2f lhs, Matrix2f rhs) {
        Matrix2f tmp = new Matrix2f();
        tmp.load(lhs);
        tmp.multiply(rhs);
        return tmp;
    }

    public static Matrix2f add(Matrix2f lhs, Matrix2f rhs) {
        Matrix2f tmp = new Matrix2f();
        tmp.load(lhs);
        tmp.set(0, 0, tmp.get(0, 0) + rhs.get(0, 0));
        tmp.set(0, 1, tmp.get(0, 1) + rhs.get(0, 1));
        tmp.set(1, 0, tmp.get(1, 0) + rhs.get(1, 0));
        tmp.set(1, 1, tmp.get(1, 1) + rhs.get(1, 1));
        return tmp;
    }
}
