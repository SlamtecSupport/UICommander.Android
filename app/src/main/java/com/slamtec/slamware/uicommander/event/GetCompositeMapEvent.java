package com.slamtec.slamware.uicommander.event;

import com.slamtec.slamware.robot.CompositeMap;

public class GetCompositeMapEvent {
    public CompositeMap getCompositeMap() {
        return compositeMap;
    }

    public void setCompositeMap(CompositeMap compositeMap) {
        this.compositeMap = compositeMap;
    }

    private CompositeMap compositeMap;

    public GetCompositeMapEvent(CompositeMap compositeMap) {
        this.compositeMap = compositeMap;
    }
}
