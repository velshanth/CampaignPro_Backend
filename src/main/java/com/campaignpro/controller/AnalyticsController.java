package com.campaignpro.controller;

import com.campaignpro.dto.AnalyticsDTO;
import com.campaignpro.model.CampaignActivity;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    private String getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.campaignpro.exception.ResourceNotFoundException("User", "username", username));
        return user.getId();
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsDTO> getOverviewAnalytics(Authentication authentication) {
        String userId = getUserId(authentication);
        AnalyticsDTO analytics = analyticsService.getOverviewAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<AnalyticsDTO>> getCampaignAnalytics(Authentication authentication) {
        String userId = getUserId(authentication);
        List<AnalyticsDTO> analytics = analyticsService.getCampaignAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<AnalyticsDTO> getCampaignAnalytics(@PathVariable String campaignId, Authentication authentication) {
        String userId = getUserId(authentication);
        AnalyticsDTO analytics = analyticsService.getCampaignAnalytics(campaignId, userId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getPerformanceTrends(Authentication authentication) {
        String userId = getUserId(authentication);
        // This would return trend data over time
        // For now, return a placeholder
        Map<String, Object> trends = Map.of(
                "message", "Trends data would be calculated here",
                "userId", userId
        );
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> generateReports(Authentication authentication) {
        String userId = getUserId(authentication);
        // This would generate detailed reports
        // For now, return a placeholder
        Map<String, Object> reports = Map.of(
                "message", "Reports would be generated here",
                "userId", userId
        );
        return ResponseEntity.ok(reports);
    }
}
