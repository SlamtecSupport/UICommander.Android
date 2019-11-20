package com.slamtec.slamware.uicommander.mapview.utils;


public class RadianUtil {

    public static float toRadians(float angel) {
        return (float) (Math.PI * angel / 180);
    }

    public static float toAngel(float radians) {
        return (float) (radians * 180 / Math.PI);
    }
}
