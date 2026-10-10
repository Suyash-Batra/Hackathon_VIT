package com.wildguard.operations.risk;

import java.util.List;

public record RiskZone(String name, String zoneCode, int score, String level,
                       List<String> reasons, String recommendedAction, long incidentCount) { }
