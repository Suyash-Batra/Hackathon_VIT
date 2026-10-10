package com.wildguard.conservation.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.service.SummaryService;
import com.wildguard.conservation.util.LoginCheck;

import jakarta.servlet.http.HttpServletRequest;

// GET /api/conservation/health  -> to see that my service is running (no login needed)
// GET /api/conservation/summary -> the numbers of my part
@RestController
@RequestMapping("/api/conservation")
@CrossOrigin(origins = "${wildguard.allowed-origins}")
public class SummaryController {

    private SummaryService summaryService;
    private LoginCheck login;

    public SummaryController(SummaryService summaryService, LoginCheck login) {
        this.summaryService = summaryService;
        this.login = login;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> answer = new LinkedHashMap<>();
        answer.put("service", "conservation-service");
        answer.put("status", "UP");
        return answer;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(HttpServletRequest request) {
        login.check(request);
        return summaryService.summary();
    }
}
