package com.slamtec.slamware.uicommander.event;

import com.slamtec.slamware.action.Path;


public class RemainingMilestonesGetEvent {
    private Path remainingMilestones;

    public RemainingMilestonesGetEvent(Path remainingMilestones) {
        this.remainingMilestones = remainingMilestones;
    }

    public Path getRemainingMilestones() {
        return remainingMilestones;
    }
}
