package com.campaignpro.controller;

import com.campaignpro.model.CampaignActivity;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/activities")
public class CampaignActivityController {

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

    @GetMapping
    public ResponseEntity<List<CampaignActivity>> getCampaignActivities(@PathVariable String campaignId, Authentication authentication) {
        String userId = getUserId(authentication);
        List<CampaignActivity> activities = analyticsService.getCampaignActivities(campaignId, userId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/opens")
    public ResponseEntity<List<CampaignActivity>> getEmailOpens(@PathVariable String campaignId, Authentication authentication) {
        String userId = getUserId(authentication);
        List<CampaignActivity> opens = analyticsService.getEmailOpens(campaignId, userId);
        return ResponseEntity.ok(opens);
    }

    @GetMapping("/clicks")
    public ResponseEntity<List<CampaignActivity>> getLinkClicks(@PathVariable String campaignId, Authentication authentication) {
        String userId = getUserId(authentication);
        List<CampaignActivity> clicks = analyticsService.getLinkClicks(campaignId, userId);
        return ResponseEntity.ok(clicks);
    }
}
