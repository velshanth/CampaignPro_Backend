package com.campaignpro.controller;

import com.campaignpro.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivityTrackingController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/track")
    public ResponseEntity<Void> trackActivity(@RequestParam String campaignId,
                                               @RequestParam String deliveryId,
                                               @RequestParam String type,
                                               @RequestParam(required = false) String url) {
        analyticsService.trackActivity(campaignId, deliveryId, type, url);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
