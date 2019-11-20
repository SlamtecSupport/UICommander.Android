package com.slamtec.slamware.uicommander.event;

public class MapUpdataEvent {
    private boolean isMapUpdata;

    public MapUpdataEvent(boolean isMapUpdata) {
        this.isMapUpdata = isMapUpdata;
    }

    public boolean isMapUpdata() {
        return this.isMapUpdata;
    }
}
