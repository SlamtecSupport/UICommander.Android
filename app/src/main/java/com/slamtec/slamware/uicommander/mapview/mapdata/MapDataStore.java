package com.slamtec.slamware.uicommander.mapview.mapdata;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Arrays;


public class MapDataStore {
    private final static String TAG = "MapDataStore";

    private int mWidth;
    private int mHeight;
    private byte[] mMapData = null;

    MapDataStore(int width, int height) {
        mWidth = width;
        mHeight = height;
        mMapData = new byte[Math.abs(width * height)];
    }

    public synchronized void update(Rect area, byte[] data) {
        if (area.height() * area.width() != data.length) {
            return;
        }

        if (mMapData == null || mMapData.length == 0) {
            mWidth = area.width();
            mHeight = area.height();
            mMapData = new byte[Math.abs(mWidth * mHeight)];
        } else {
            copyBuffer(mMapData, new Rect(0, 0, mWidth, mHeight), new Point(area.left, area.top), data, new Rect(0, 0, area.width(), area.height()), new Point(0, 0), new Point(area.width(), area.height()));
        }
    }

    public synchronized void fetch(Rect area, byte[] buffer) {
        Arrays.fill(buffer, (byte) 0);
        copyBuffer(buffer, new Rect(0, 0, area.width(), area.height()), new Point(0, 0), mMapData, new Rect(0, 0, mWidth, mHeight), new Point(area.left, area.top), new Point(area.width(), area.height()));
    }

    public synchronized Rect expandArea(Rect area) {
        int oldWidth = mWidth;
        int oldHeight = mHeight;

        if (area.right > mWidth) mWidth = area.right;
        if (area.bottom > mHeight) mHeight = area.bottom;

        if (mWidth == oldWidth && mHeight == oldHeight) {
            return area;
        }

        byte[] newBuffer = new byte[Math.abs(mWidth * mHeight)];

        if (mMapData == null) {
            this.mMapData = newBuffer;
        } else {
            copyBuffer(newBuffer, new Rect(0, 0, mWidth, mHeight), new Point(mWidth - oldWidth, mHeight - oldHeight), mMapData, new Rect(0, 0, oldWidth, oldHeight), new Point(0, 0), new Point(oldWidth, oldHeight));
            this.mMapData = newBuffer;
        }

        return new Rect(0, 0, mWidth, mHeight);
    }

    private synchronized static void copyBuffer(byte[] dest, Rect destSize, Point destOffset, byte[] src, Rect srcSize, Point srcOffset, Point dimension) {
        if (dest == null || src == null) {
            return;
        }

        int destWidth = destSize.width();
        int srcWidth = srcSize.width();
        int copySize = dimension.x;

        int srcIndex = srcSize.width() * srcOffset.y + srcOffset.x;
        int destIndex = destSize.width() * destOffset.y + destOffset.x;

        for (int y = 0; y < dimension.y; y++) {
            System.arraycopy(src, srcIndex, dest, destIndex, copySize);
            destIndex += destWidth;
            srcIndex += srcWidth;
        }
    }

    public synchronized void clear() {
        mWidth = 0;
        mHeight = 0;
        mMapData = null;
    }

    public boolean isEmpty() {
        return this.mMapData == null;
    }

    public byte get(int x, int y) {
        if (x < 0 || y < 0 || x >= mWidth || y >= mHeight) {
            return 0;
        }

        return mMapData[y * mWidth + x];
    }

    public void set(int x, int y, byte color) {
        if (x < 0 || y < 0 || x >= mWidth || y >= mHeight) return;

        mMapData[y * mWidth + x] = color;
    }

    public Rect getArea() {
        return new Rect(0, 0, mWidth, mHeight);
    }

    public byte[] getmMapData() {
        return this.mMapData;
    }

}
