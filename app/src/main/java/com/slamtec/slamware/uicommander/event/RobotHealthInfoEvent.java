package com.slamtec.slamware.uicommander.event;

import java.util.List;

public class RobotHealthInfoEvent {
    private String errorMsg = "";
    private List<Integer> errorList;

    public RobotHealthInfoEvent(String errorMsg, List<Integer> errorList) {
        this.errorMsg = errorMsg;
        this.errorList = errorList;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<Integer> getErrorList() {
        return errorList;
    }
}
