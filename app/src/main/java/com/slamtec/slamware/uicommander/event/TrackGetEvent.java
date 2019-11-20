package com.slamtec.slamware.uicommander.event;

import com.slamtec.slamware.geometry.Line;

import java.util.List;

public class TrackGetEvent {
    private List<Line> tracks;

    public TrackGetEvent(List<Line> tracks) {
        this.tracks = tracks;
    }

    public List<Line> getTracks() {
        return tracks;
    }
}

