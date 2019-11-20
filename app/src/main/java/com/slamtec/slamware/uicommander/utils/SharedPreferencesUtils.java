package com.slamtec.slamware.uicommander.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class SharedPreferencesUtils {

    public static String readString(Context context, String key, String v) {
        return readString(context, key, v, Context.MODE_PRIVATE);
    }

    public static String readString(Context context, String key, String v, int mode) {
        return context.getSharedPreferences(context.getPackageName(), mode).getString(key, v);
    }

    public static void writeString(Context context, String key, String v) {
        writeString(context, key, v, Context.MODE_PRIVATE);
    }

    public static void writeString(Context context, String key, String v, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().putString(key, v).apply();
    }

    public static int readInt(Context context, String key, int v) {
        return readInt(context, key, v, Context.MODE_PRIVATE);
    }

    public static int readInt(Context context, String key, int v, int mode) {
        return context.getSharedPreferences(context.getPackageName(), mode).getInt(key, v);
    }

    public static void writeInt(Context context, String key, int v) {
        writeInt(context, key, v, Context.MODE_PRIVATE);
    }

    public static void writeInt(Context context, String key, int v, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().putInt(key, v).apply();
    }

    public static long readLong(Context context, String key, long v) {
        return readLong(context, key, v, Context.MODE_PRIVATE);
    }

    public static long readLong(Context context, String key, long v, int mode) {
        return context.getSharedPreferences(context.getPackageName(), mode).getLong(key, v);
    }

    public static void writeLong(Context context, String key, long v) {
        writeLong(context, key, v, Context.MODE_PRIVATE);
    }

    public static void writeLong(Context context, String key, long v, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().putLong(key, v).apply();
    }

    public static float readFloat(Context context, String key, float v) {
        return readFloat(context, key, v, Context.MODE_PRIVATE);
    }

    public static float readFloat(Context context, String key, float v, int mode) {
        return context.getSharedPreferences(context.getPackageName(), mode).getFloat(key, v);
    }

    public static void writeFloat(Context context, String key, float v) {
        writeFloat(context, key, v, Context.MODE_PRIVATE);
    }

    public static void writeFloat(Context context, String key, float v, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().putFloat(key, v).apply();
    }

    public static boolean readBoolean(Context context, String key, boolean v) {
        return readBoolean(context, key, v, Context.MODE_PRIVATE);
    }

    public static boolean readBoolean(Context context, String key, boolean v, int mode) {
        return context.getSharedPreferences(context.getPackageName(), mode).getBoolean(key, v);
    }

    public static void writeBoolean(Context context, String key, boolean v) {
        writeBoolean(context, key, v, Context.MODE_PRIVATE);
    }

    public static void writeBoolean(Context context, String key, boolean v, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().putBoolean(key, v).apply();
    }

    public static void removeKey(Context context, String key) {
        removeKey(context, key, Context.MODE_PRIVATE);
    }

    public static void removeKey(Context context, String key, int mode) {
        context.getSharedPreferences(context.getPackageName(), mode).edit().remove(key).apply();
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static <T extends Serializable> void writeObject(Context context, String key, T obj) {
        try {
            writeObject(context, key, obj, Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T extends Serializable> void writeObject(Context context, String key, T obj, int modePrivate) throws IOException {
        if (obj == null) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);

        String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        baos.close();
        oos.close();

        writeString(context, key, objectStr, modePrivate);

    }

    public static <T extends Serializable> T readObject(Context context, String key, T obj) {
        try {
            return (T) readObject(context, key, "", Context.MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static Object readObject(Context context, String key, String v, int mode) throws IOException, ClassNotFoundException {

        String wordBase64 = readString(context, key, v, mode);

        if (TextUtils.isEmpty(wordBase64)) {
            return null;
        }
        byte[] objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        Object obj = ois.readObject();
        bais.close();
        ois.close();
        return obj;
    }
}

