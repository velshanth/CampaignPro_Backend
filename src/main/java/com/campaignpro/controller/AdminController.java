package com.campaignpro.controller;

import com.campaignpro.dto.AnalyticsDTO;
import com.campaignpro.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboard = adminService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersWithStats() {
        List<Map<String, Object>> users = adminService.getAllUsersWithStats();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<Map<String, Object>>> getAllCampaignsWithStats() {
        List<Map<String, Object>> campaigns = adminService.getAllCampaignsWithStats();
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Map<String, Object>>> getSystemActivities() {
        List<Map<String, Object>> activities = adminService.getSystemActivities();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDTO> getPlatformAnalytics() {
        AnalyticsDTO analytics = adminService.getPlatformAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getSystemLogs() {
        List<Map<String, Object>> logs = adminService.getSystemLogs();
        return ResponseEntity.ok(logs);
    }
}
