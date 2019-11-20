package com.slamtec.slamware.uicommander.event;

public class RobotInfoEvent {
    private int modelId;
    private String hardwareVersion;
    private String softwareVersion;
    private String modelName;

    public RobotInfoEvent(int modelId, String hardwareVersion, String softwareVersion, String modelName) {
        this.modelId = modelId;
        this.hardwareVersion = hardwareVersion;
        this.softwareVersion = softwareVersion;
        this.modelName = modelName;
    }

    public int getModelId() {
        return modelId;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getModelName() {
        return modelName;
    }
}
