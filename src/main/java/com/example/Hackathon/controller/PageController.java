package com.example.Hackathon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin_dashboard";
    }
    
    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }
    
    @GetMapping("/risk-radar")
    public String riskRadar() {
        return "risk-radar";
    }
}