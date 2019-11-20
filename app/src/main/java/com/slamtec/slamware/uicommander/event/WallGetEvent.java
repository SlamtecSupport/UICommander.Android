package com.slamtec.slamware.uicommander.event;

import com.slamtec.slamware.geometry.Line;

import java.util.List;

public class WallGetEvent {
    private List<Line> walls;

    public WallGetEvent(List<Line> walls) {
        this.walls = walls;
    }

    public List<Line> getWalls() {
        return walls;
    }
}

