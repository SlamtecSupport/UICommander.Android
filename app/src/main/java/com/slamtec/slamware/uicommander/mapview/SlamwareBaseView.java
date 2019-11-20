package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.ViewGroup;

import com.slamtec.slamware.uicommander.mapview.utils.RadianUtil;

import java.lang.ref.WeakReference;

public abstract class SlamwareBaseView extends ViewGroup {
    private static final String TAG = "SlamwareBaseView";
    protected WeakReference<MapView> mParent;
    protected float mScale;
    protected float mRotation;
    protected Matrix mMatrix;

    public SlamwareBaseView(Context context, WeakReference<MapView> parent) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        setWillNotDraw(false);

        this.mParent = parent;
        this.mScale = 1.0f;
        this.mRotation = 0f;
        mMatrix = new Matrix();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // intentionally empty
    }

    public float getScale() {
        return mScale;
    }

    public void setMatrix(Matrix mMatrix) {
        this.mMatrix = mMatrix;
        invalidate();
    }

    public void setMatrixWithScale(Matrix matrix, float scale) {
        this.mMatrix = matrix;
        this.mScale = scale;
        invalidate();
    }

    public void setMatrixWithRotation(Matrix matrix, float rotation) {
        this.mMatrix = matrix;
        this.mRotation += RadianUtil.toAngel(rotation);
        invalidate();
    }

}
