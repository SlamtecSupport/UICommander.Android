package com.slamtec.slamware.uicommander.event;


public class RobotStatusGetEvent {
    private int batteryPercentage;
    private boolean isCharging;
    private int localizationQuality;

    public RobotStatusGetEvent(int batteryPercentage, boolean isCharging, int localizationQuality) {
        this.batteryPercentage = batteryPercentage;
        this.isCharging = isCharging;
        this.localizationQuality = localizationQuality;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public int getLocalizationQuality() {
        return localizationQuality;
    }
}
