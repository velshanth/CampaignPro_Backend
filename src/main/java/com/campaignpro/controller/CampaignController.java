package com.campaignpro.controller;

import com.campaignpro.dto.CampaignDTO;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private UserRepository userRepository;

    private String getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.campaignpro.exception.ResourceNotFoundException("User", "username", username));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<CampaignDTO>> getAllCampaigns(Authentication authentication) {
        String userId = getUserId(authentication);
        List<CampaignDTO> campaigns = campaignService.getAllCampaigns(userId);
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> getCampaignById(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        CampaignDTO campaign = campaignService.getCampaignById(id, userId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping
    public ResponseEntity<CampaignDTO> createCampaign(@Valid @RequestBody CampaignDTO campaignDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        CampaignDTO createdCampaign = campaignService.createCampaign(userId, campaignDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCampaign);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignDTO> updateCampaign(@PathVariable String id, @Valid @RequestBody CampaignDTO campaignDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        CampaignDTO updatedCampaign = campaignService.updateCampaign(id, userId, campaignDTO);
        return ResponseEntity.ok(updatedCampaign);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        campaignService.deleteCampaign(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendCampaign(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        campaignService.sendCampaign(id, userId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Void> scheduleCampaign(@PathVariable String id, @RequestBody Map<String, String> scheduleData, Authentication authentication) {
        String userId = getUserId(authentication);
        LocalDateTime scheduledTime = LocalDateTime.parse(scheduleData.get("scheduledTime"));
        campaignService.scheduleCampaign(id, userId, scheduledTime);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelScheduledCampaign(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        campaignService.cancelScheduledCampaign(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<CampaignDTO> getCampaignStatus(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        CampaignDTO campaign = campaignService.getCampaignById(id, userId);
        return ResponseEntity.ok(campaign);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CampaignDTO>> getCampaignHistory(Authentication authentication) {
        String userId = getUserId(authentication);
        List<CampaignDTO> history = campaignService.getCampaignHistory(userId);
        return ResponseEntity.ok(history);
    }
}
