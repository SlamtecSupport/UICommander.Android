package com.slamtec.slamware.uicommander.mapview.utils;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.Queue;

public class MathUtils {

    private static abstract class ObjectsPool<T> {

        private int mSize;

        private Queue<T> mQueue;

        public ObjectsPool(int size) {
            mSize = size;
            mQueue = new LinkedList<T>();
        }

        public T take() {
            if (mQueue.size() == 0) {
                return newInstance();
            } else {
                return resetInstance(mQueue.poll());
            }
        }

        public void given(T obj) {
            if (obj != null && mQueue.size() < mSize) {
                mQueue.offer(obj);
            }
        }

        abstract protected T newInstance();

        abstract protected T resetInstance(T obj);
    }

    private static class MatrixPool extends ObjectsPool<Matrix> {

        public MatrixPool(int size) {
            super(size);
        }

        @Override
        protected Matrix newInstance() {
            return new Matrix();
        }

        @Override
        protected Matrix resetInstance(Matrix obj) {
            obj.reset();
            return obj;
        }
    }

    private static class RectFPool extends ObjectsPool<RectF> {

        public RectFPool(int size) {
            super(size);
        }

        @Override
        protected RectF newInstance() {
            return new RectF();
        }

        @Override
        protected RectF resetInstance(RectF obj) {
            obj.setEmpty();
            return obj;
        }
    }

    private static MatrixPool mMatrixPool = new MatrixPool(16);

    public static Matrix matrixTake() {
        return mMatrixPool.take();
    }

    public static Matrix matrixTake(Matrix matrix) {
        Matrix result = mMatrixPool.take();
        if (matrix != null) {
            result.set(matrix);
        }
        return result;
    }

    public static void matrixGiven(Matrix matrix) {
        mMatrixPool.given(matrix);
    }

    private static RectFPool mRectFPool = new RectFPool(16);

    public static RectF rectFTake() {
        return mRectFPool.take();
    }

    public static RectF rectFTake(float left, float top, float right, float bottom) {
        RectF result = mRectFPool.take();
        result.set(left, top, right, bottom);
        return result;
    }

    public static RectF rectFTake(RectF rectF) {
        RectF result = mRectFPool.take();
        if (rectF != null) {
            result.set(rectF);
        }
        return result;
    }

    public static void rectFGiven(RectF rectF) {
        mRectFPool.given(rectF);
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float[] getCenterPoint(float x1, float y1, float x2, float y2) {
        return new float[]{(x1 + x2) / 2f, (y1 + y2) / 2f};
    }

    public static float[] getMatrixScale(Matrix matrix) {
        if (matrix != null) {
            float[] value = new float[9];
            matrix.getValues(value);
            return new float[]{value[0], value[4]};
        } else {
            return new float[2];
        }
    }

    public static float[] inverseMatrixPoint(Matrix matrix, float[] point) {
        if (point != null && matrix != null) {
            float[] dst = new float[2];
            Matrix inverse = matrixTake();
            matrix.invert(inverse);
            inverse.mapPoints(dst, point);
            matrixGiven(inverse);
            return dst;
        } else {
            return new float[2];
        }
    }

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

    public static void calculateScaledRectInContainer(RectF container, float srcWidth, float srcHeight, ImageView.ScaleType scaleType, RectF result) {
        if (container == null || result == null) {
            return;
        }
        if (srcWidth == 0 || srcHeight == 0) {
            return;
        }

        if (scaleType == null) {
            scaleType = ImageView.ScaleType.FIT_CENTER;
        }
        result.setEmpty();
        if (ImageView.ScaleType.FIT_XY.equals(scaleType)) {
            result.set(container);
        } else if (ImageView.ScaleType.CENTER.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            matrix.setTranslate((container.width() - srcWidth) * 0.5f, (container.height() - srcHeight) * 0.5f);
            matrix.mapRect(result, rect);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else if (ImageView.ScaleType.CENTER_CROP.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            float scale;
            float dx = 0;
            float dy = 0;
            if (srcWidth * container.height() > container.width() * srcHeight) {
                scale = container.height() / srcHeight;
                dx = (container.width() - srcWidth * scale) * 0.5f;
            } else {
                scale = container.width() / srcWidth;
                dy = (container.height() - srcHeight * scale) * 0.5f;
            }
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
            matrix.mapRect(result, rect);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else if (ImageView.ScaleType.CENTER_INSIDE.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            float scale;
            float dx;
            float dy;
            if (srcWidth <= container.width() && srcHeight <= container.height()) {
                scale = 1f;
            } else {
                scale = Math.min(container.width() / srcWidth, container.height() / srcHeight);
            }
            dx = (container.width() - srcWidth * scale) * 0.5f;
            dy = (container.height() - srcHeight * scale) * 0.5f;
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
            matrix.mapRect(result, rect);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else if (ImageView.ScaleType.FIT_CENTER.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempDst = rectFTake(0, 0, container.width(), container.height());
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);
            matrix.mapRect(result, rect);
            rectFGiven(tempDst);
            rectFGiven(tempSrc);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else if (ImageView.ScaleType.FIT_START.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempDst = rectFTake(0, 0, container.width(), container.height());
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START);
            matrix.mapRect(result, rect);
            rectFGiven(tempDst);
            rectFGiven(tempSrc);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else if (ImageView.ScaleType.FIT_END.equals(scaleType)) {
            Matrix matrix = matrixTake();
            RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
            RectF tempDst = rectFTake(0, 0, container.width(), container.height());
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.END);
            matrix.mapRect(result, rect);
            rectFGiven(tempDst);
            rectFGiven(tempSrc);
            rectFGiven(rect);
            matrixGiven(matrix);
            result.left += container.left;
            result.right += container.left;
            result.top += container.top;
            result.bottom += container.top;
        } else {
            result.set(container);
        }
    }

    public static float calculateDistance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static float calculateDistance(float x0, float y0, float x1, float y1) {
        return (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
    }

    public static float calculateDistance(com.slamtec.slamware.geometry.PointF p1, com.slamtec.slamware.geometry.PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

}