package com.slamtec.slamware.uicommander.mapview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.slamtec.slamware.action.Path;
import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Map;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.uicommander.R;
import com.slamtec.slamware.uicommander.mapview.mapdata.MapDataCache;
import com.slamtec.slamware.uicommander.mapview.mapdata.MapDataColor;
import com.slamtec.slamware.uicommander.mapview.utils.MathUtils;
import com.slamtec.slamware.uicommander.mapview.utils.SlamGestureDetector;
import com.slamtec.slamware.uicommander.mapview.utils.RadianUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.slamtec.slamware.uicommander.mapview.utils.MathUtils.inverseMatrixPoint;

public class MapView extends FrameLayout implements SlamGestureDetector.OnRPGestureListener {
    private final static String TAG = MapView.class.getName();

    // View argc
    private Matrix mOuterMatrix = new Matrix();
    private int VIEW_WITCH = 0;
    private int VIEW_HEIGHT = 0;
    private float mMapScale = 1;
    private float mMaxMapScale = 400;
    private float mMinMapScale = 0.1f;

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private MapDataCache mMapData;
    private WeakReference<MapView> mMapView;

    private ArrayList<SlamwareBaseView> mapLayers = new ArrayList<>(10);
    private SlamMapView mSlamMapView;
    private VirtualLineView mWallView;
    private VirtualLineView mTrackView;
    private LaserScanView mLaserScanView;
    private RemainingMilestonesView mRemainingMilestonesView;
    private RemainingPathView mRemainingPathView;
    private DeviceView mDeviceView;
    private HomeDockView mHomeDockView;
    private TradeMarkView mTradeMarkView;

    private ISingleTapListener mSingleTapListener;
    private SlamGestureDetector mGestureDetector;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setDefaultBackground();
        mOuterMatrix = new Matrix();
        mGestureDetector = new SlamGestureDetector(this, this);
        initView();
    }

    private void setDefaultBackground() {
        int backgroud = MapDataColor.RGB_GREY;
        setBackgroundColor(Color.argb(0xFF, backgroud, backgroud, backgroud));
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mMapView = new WeakReference<>(this);
        mMapData = new MapDataCache();

        mSlamMapView = new SlamMapView(getContext());
        mWallView = new VirtualLineView(getContext(), mMapView, Color.RED);
        mTrackView = new VirtualLineView(getContext(), mMapView, Color.GREEN);
        mLaserScanView = new LaserScanView(getContext(), mMapView);
        mRemainingMilestonesView = new RemainingMilestonesView(getContext(), mMapView);
        mRemainingPathView = new RemainingPathView(getContext(), mMapView);
        mDeviceView = new DeviceView(getContext(), mMapView);
        mHomeDockView = new HomeDockView(getContext(), mMapView);
        mTradeMarkView = new TradeMarkView(getContext());

        addView(mSlamMapView, lp);
        addMapLayers(mWallView);
        addMapLayers(mTrackView);
        addMapLayers(mHomeDockView);
        addMapLayers(mLaserScanView);
        addMapLayers(mRemainingPathView);
        addMapLayers(mRemainingMilestonesView);
        addMapLayers(mDeviceView);
        addView(mTradeMarkView);
        setCentred();
    }

    /////////////////////////////////////// setter /////////////////////////////////////////////////
    private int mCnt = 0;

    public void setMap(Map map) {
        mExecutorService.execute(() -> {
            if (map == null) return;
            mMapData = new MapDataCache(map);
            mSlamMapView.updateTiles(mMapData);
            if (mCnt++ == 0) setCentred();
        });
    }

    public void setVwalls(List<Line> walls) {
        mWallView.setLines(walls);
    }

    public void setVtracks(List<Line> tracks) {
        mTrackView.setLines(tracks);
    }

    public void setLaserScan(LaserScan laserScan) {
        mLaserScanView.updateLaserScan(laserScan);
    }

    public void setRemainingMilestones(Path remainingMilestones) {
        mRemainingMilestonesView.updateRemainingMilestones(remainingMilestones);
    }

    public void setRemainingPath(Path remainingPath) {
        mRemainingPathView.updateRemainingPath(remainingPath);
    }

    public void setRobotPose(Pose pose) {
        mDeviceView.setDevicePose(pose);
    }

    public void setHomePose(Pose homePose) {
        mHomeDockView.setHomePose(homePose);
    }


    // 以下为触摸事件 ///////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onMapTap(MotionEvent event) {
        singleTap(event);
    }

    @Override
    public void onMapPinch(float factor, PointF center) {
        setScale(factor, center.x, center.y);
    }

    @Override
    public void onMapMove(int distanceX, int distanceY) {
        setTransition(distanceX, distanceY);
    }

    @Override
    public void onMapRotate(float factor, PointF center) {
        setRotation(factor, (int) center.x, (int) center.y);
    }

    private void setRotation(float factor, int cx, int cy) {
        mOuterMatrix.postRotate(RadianUtil.toAngel(factor), cx, cy);
        setMatrixWithRotation(mOuterMatrix, factor);
    }

    private void setTransition(int dx, int dy) {
        mOuterMatrix.postTranslate(dx, dy);
        this.setMatrix(mOuterMatrix);
    }

    private void setScale(float factor, float cx, float cy) {
        float scale = mMapScale * factor;
        if (scale > mMaxMapScale || scale < mMinMapScale) return;
        mMapScale = scale;
        mOuterMatrix.postScale(factor, factor, cx, cy);
        setMatrixWithScale(mOuterMatrix, mMapScale);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void singleTap(MotionEvent event) {
        if (mSingleTapListener != null) {
            mSingleTapListener.onSingleTapListener(event);
        }
    }

    private void setMatrix(Matrix matrix) {
        mSlamMapView.setMatrix(matrix);
        for (SlamwareBaseView mapLayer : mapLayers) {
            mapLayer.setMatrix(matrix);
        }

        postInvalidate();
    }

    private void setMatrixWithScale(Matrix matrix, float scale) {
        this.mOuterMatrix = matrix;
        this.mMapScale = scale;
        mSlamMapView.setMatrix(matrix);

        for (SlamwareBaseView mapLayer : mapLayers) {
            mapLayer.setMatrixWithScale(matrix, scale);
        }
    }

    private void setMatrixWithScaleAndRotation(Matrix matrix, float scale, float rotation) {
        this.mOuterMatrix = matrix;
        this.mMapScale = scale;
        mSlamMapView.setMatrix(matrix);

        for (SlamwareBaseView mapLayer : mapLayers) {
            mapLayer.setMatrixWithScale(matrix, scale);
            mapLayer.mRotation = rotation;
        }
    }

    private void setMatrixWithRotation(Matrix matrix, float rotation) {
        this.mOuterMatrix = matrix;
        mSlamMapView.setMatrix(matrix);

        for (SlamwareBaseView mapLayer : mapLayers) {
            mapLayer.setMatrixWithRotation(matrix, rotation);
        }
    }

    public void setCentred() {
        RectF scaledRect = new RectF();

        if (VIEW_WITCH == 0 || VIEW_HEIGHT == 0) {
            this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    VIEW_HEIGHT = getHeight();
                    VIEW_WITCH = getWidth();
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    setCentred();
                }
            });
            return;
        }

        if (mMapData != null && mMapData != null) {
            int iWidth = mMapData.getmDimension().getWidth();
            int iHeight = mMapData.getmDimension().getHeight();

            MathUtils.calculateScaledRectInContainer(new RectF(0, 0, VIEW_WITCH, VIEW_HEIGHT), iWidth, iHeight, ImageView.ScaleType.FIT_CENTER, scaledRect);
            float scale = scaledRect.width() / (float) iWidth;
            mMinMapScale = scale / 4;
            mMapScale = scale;

            mOuterMatrix = new Matrix();
            mOuterMatrix.postScale(mMapScale, mMapScale);
            mOuterMatrix.postTranslate((VIEW_WITCH - mMapScale * iWidth) / 2, (VIEW_HEIGHT - mMapScale * iHeight) / 2);
            setMatrixWithScaleAndRotation(mOuterMatrix, mMapScale, 0f);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        VIEW_WITCH = MeasureSpec.getSize(widthMeasureSpec);
        VIEW_HEIGHT = MeasureSpec.getSize(heightMeasureSpec);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    protected com.slamtec.slamware.geometry.PointF widghtCoordinateToMapCoordinate(float x, float y) {
        Matrix m = mOuterMatrix;
        float[] eventPoint = new float[]{x, y};
        float[] values = inverseMatrixPoint(m, eventPoint);
        return mMapData.mapPixelCoordinateToMapCoordinate(new PointF(values[0], values[1]));
    }

    public Point widgetCoordinateToMapPixelCoordinate(float x, float y) {
        Matrix m = mOuterMatrix;
        float[] eventPoint = new float[]{x, y};
        float[] values = inverseMatrixPoint(m, eventPoint);
        return new Point((int) values[0], (int) values[1]);
    }

    public PointF widgetCoordinateToMapPixelCoordinateF(float x, float y) {
        Matrix m = mOuterMatrix;
        float[] eventPoint = new float[]{x, y};
        float[] values = inverseMatrixPoint(m, eventPoint);
        return new PointF(values[0], values[1]);
    }

    public com.slamtec.slamware.geometry.PointF widgetCoordinateToMapCoordinate(PointF widgetPointF) {
        Matrix m = mOuterMatrix;
        float[] points = new float[]{widgetPointF.x, widgetPointF.y};
        float[] values = inverseMatrixPoint(m, points);
        com.slamtec.slamware.geometry.PointF point = mMapData.mapPixelCoordinateToMapCoordinate(new PointF(values[0], values[1]));
        return new com.slamtec.slamware.geometry.PointF(point.getX(), point.getY());
    }

    public com.slamtec.slamware.geometry.PointF widgetCoordinateToMapCoordinate(float x, float y) {
        Matrix m = mOuterMatrix;
        float[] points = new float[]{x, y};
        float[] values = inverseMatrixPoint(m, points);
        com.slamtec.slamware.geometry.PointF point = mMapData.mapPixelCoordinateToMapCoordinate(new PointF(values[0], values[1]));
        return new com.slamtec.slamware.geometry.PointF(point.getX(), point.getY());
    }

    public Point widgetCoordinateToMapPixelCoordinate(int x, int y) {
        Matrix m = mOuterMatrix;
        float[] points = new float[]{x, y};
        float[] values = inverseMatrixPoint(m, points);
        return new Point((int) values[0], (int) values[1]);
    }

    public float mapDistanceToWidghtDistance(float distence) {
        synchronized (mMapData) {
            float mapPixelDistance = mMapData.mapDistanceToMapPixelDistance(distence);
            return mapPixelDistance * mMapScale;
        }
    }

    public PointF mapCoordinateToWidghtCoordinateF(float x, float y) {
        synchronized (mMapData) {
            PointF mapPixelPointF = mMapData.mapCoordinateToMapPixelCoordinateF(x, y);
            return mapPixelCoordinateToMapWidghtCoordinateF(mapPixelPointF);
        }
    }

    public PointF mapCoordinateToWidghtCoordinateF(com.slamtec.slamware.geometry.PointF mapPointF) {
        synchronized (mMapData) {
            PointF mapPixelPointF = mMapData.mapCoordinateToMapPixelCoordinateF(mapPointF.getX(), mapPointF.getY());
            return mapPixelCoordinateToMapWidghtCoordinateF(mapPixelPointF);
        }
    }

    public Point mapCoordinateToWidghtCoordinate(com.slamtec.slamware.geometry.PointF mapPointF) {
        synchronized (mMapData) {
            PointF mapPixelPointF = mMapData.mapCoordinateToMapPixelCoordinateF(mapPointF.getX(), mapPointF.getY());
            PointF p = mapPixelCoordinateToMapWidghtCoordinateF(mapPixelPointF);
            return new Point((int) p.x, (int) p.y);
        }

    }

    public PointF mapPixelCoordinateToMapWidghtCoordinateF(PointF mapPixelPointF) {
        Matrix m = mOuterMatrix;
        float[] points = new float[]{mapPixelPointF.x, mapPixelPointF.y};
        m.mapPoints(points);
        return new PointF(points[0], points[1]);
    }

    public PointF mapCoordinateToMapPixelCoordinateF(com.slamtec.slamware.geometry.PointF mapPointF) {
        PointF mapPixelPointF = mMapData.mapCoordinateToMapPixelCoordinateF(mapPointF.getX(), mapPointF.getY());
        return mapPixelPointF;
    }

    public PointF mapCoordinateToMapPixelCoordinateF(float x, float y) {
        if (mMapData == null) {
            return null;
        } else {
            return mMapData.mapCoordinateToMapPixelCoordinateF(x, y);
        }
    }

    @Override
    public void invalidate() {
        super.postInvalidate();
        mSlamMapView.postInvalidate();

        for (SlamwareBaseView mapLayer : mapLayers) {
            mapLayer.postInvalidate();
        }
    }

    public void addMapLayers(SlamwareBaseView mapLayer) {
        if (mapLayer == null) return;
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (!mapLayers.contains(mapLayer)) {
            mapLayers.add(mapLayer);
            addView(mapLayer, lp);
        }
    }

    public void setSingleTapListener(ISingleTapListener singleTapListener) {
        mSingleTapListener = singleTapListener;
    }

    public interface ISingleTapListener {
        void onSingleTapListener(MotionEvent event);
    }

}
