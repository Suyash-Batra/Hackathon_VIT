package com.wildguard.conservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.wildguard.conservation.entity.RescueCase.RescueStage;

// Small unit tests for the rules of my service. They dont need the database.
class ConservationRulesTest {

    // progress = completed / target * 100
    @Test
    void progressIsCalculatedFromCompletedAndTarget() {
        assertEquals(60, ProgrammeService.progressPercent(12, 20)); // demo: 12 of 20 water sources
        assertEquals(0, ProgrammeService.progressPercent(0, 20));
        assertEquals(33, ProgrammeService.progressPercent(1, 3));   // rounded
        assertEquals(100, ProgrammeService.progressPercent(25, 20)); // not more than 100
    }

    // checking the allowed moves between rescue stages
    @Test
    void rescueStagesFollowTheRightOrder() {
        assertTrue(RescueStage.REPORTED.nextStages().contains(RescueStage.RESCUED));
        assertFalse(RescueStage.REPORTED.nextStages().contains(RescueStage.RELEASED));
        assertTrue(RescueStage.REHABILITATION.nextStages().contains(RescueStage.RELEASED));
        assertTrue(RescueStage.RELEASED.nextStages().isEmpty());
        assertTrue(RescueStage.UNDER_TREATMENT.isActive());
        assertFalse(RescueStage.TRANSFERRED.isActive());
    }
}
