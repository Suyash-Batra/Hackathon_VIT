package com.wildguard.conservation.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.dto.ConservationSummary;
import com.wildguard.conservation.service.ConservationSummaryService;

// Two small urls:
//   GET /api/conservation/health  -> to check that my service is running (no login needed)
//   GET /api/conservation/summary -> the numbers of my part for a dashboard
@RestController
@RequestMapping("/api/conservation")
public class SummaryController {

    private ConservationSummaryService summaryService;

    public SummaryController(ConservationSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> answer = new LinkedHashMap<>();
        answer.put("service", "conservation-service");
        answer.put("status", "UP");
        return answer;
    }

    @GetMapping("/summary")
    public ConservationSummary summary() {
        return summaryService.summary();
    }
}
