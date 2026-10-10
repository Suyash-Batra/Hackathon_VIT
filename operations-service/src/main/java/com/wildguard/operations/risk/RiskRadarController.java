package com.wildguard.operations.risk;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/risk-radar")
public class RiskRadarController {
    private final RiskRadarService service;
    public RiskRadarController(RiskRadarService service) { this.service = service; }
    @GetMapping("/zones")
    public List<RiskZone> zones() { return service.getZones(); }
}
