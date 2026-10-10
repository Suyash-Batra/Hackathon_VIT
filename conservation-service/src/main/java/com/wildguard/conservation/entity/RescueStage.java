package com.wildguard.conservation.entity;

import java.util.ArrayList;
import java.util.List;

// The stages a rescued animal goes through.
// REPORTED -> RESCUED -> UNDER_TREATMENT -> REHABILITATION -> RELEASED or TRANSFERRED
public enum RescueStage {
    REPORTED, RESCUED, UNDER_TREATMENT, REHABILITATION, RELEASED, TRANSFERRED;

    // case is still open if the animal is not released or transferred yet
    public boolean isActive() {
        return this != RELEASED && this != TRANSFERRED;
    }

    // tells which stages we are allowed to go to from the current stage
    public List<RescueStage> nextStages() {
        List<RescueStage> next = new ArrayList<>();
        if (this == REPORTED) {
            next.add(RESCUED);
        } else if (this == RESCUED) {
            next.add(UNDER_TREATMENT);
            next.add(TRANSFERRED);
        } else if (this == UNDER_TREATMENT) {
            next.add(REHABILITATION);
            next.add(TRANSFERRED);
        } else if (this == REHABILITATION) {
            next.add(RELEASED);
            next.add(TRANSFERRED);
            next.add(UNDER_TREATMENT); // animal got worse so back to treatment
        }
        // RELEASED and TRANSFERRED are the end, so the list stays empty
        return next;
    }
}
