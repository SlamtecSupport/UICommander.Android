package com.slamtec.slamware.uicommander.mapview.utils;

import android.graphics.Bitmap;

import com.slamtec.slamware.uicommander.mapview.mapdata.MapDataColor;


public class ImageUtil {
    private final static String TAG = "ImageUtil";

    private ImageUtil() {
    }

    public static Bitmap createImage(byte[] buffer, int width, int height) {
        int[] rawData = new int[buffer.length];
        int alpha;

        for (int i = 0; i < buffer.length; i++) {
            int grey = 0x80 + buffer[i];
            grey = MapDataColor.GREY2RGB_TABLE[grey];
            alpha = (grey == 127) ? 0 : 0xFF;

            rawData[i] = alpha << 24 | grey << 16 | grey << 8 | grey;
        }
        return Bitmap.createBitmap(rawData, width, height, Bitmap.Config.ARGB_8888);
    }
}
