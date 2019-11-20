package com.slamtec.slamware.uicommander.event;

import com.slamtec.slamware.robot.Pose;

public class RobotPoseGetEvent {
    private Pose pose;

    public RobotPoseGetEvent(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }
}
