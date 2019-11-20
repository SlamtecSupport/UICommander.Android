package com.slamtec.slamware.uicommander.mapview.mapdata;

import static java.lang.Math.exp;


public class MapDataColor {
    private final static int TABLE_SIZE = 256;

    public static final byte GREY = 0;
    public static final byte WHITE = 127;
    public static final byte BLACK = -127;

    public static final int RGB_GREY;
    public static final int RGB_WHITE;
    public static final int RGB_BLACK;

    public static int GREY2RGB_TABLE[] = new int[TABLE_SIZE];

    static {
        // The factor for converting log2-odds into integers
        final double LOGODD_K = 16;
        final double LOGODD_K_INV = 1.0 / LOGODD_K;

        for (int i = 0; i < TABLE_SIZE; i++) {
            float f = (float) (1.0f / (1.0f + exp((127 - i) * LOGODD_K_INV)));
            GREY2RGB_TABLE[i] = (int) (f * 255.0f);
        }

        RGB_GREY = GREY2RGB_TABLE[GREY + 0x80];
        RGB_WHITE = GREY2RGB_TABLE[WHITE + 0x80];
        RGB_BLACK = GREY2RGB_TABLE[BLACK + 0x80];
    }

    private MapDataColor() {
    }

}
