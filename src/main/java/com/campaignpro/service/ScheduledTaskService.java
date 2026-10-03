package com.campaignpro.service;

import com.campaignpro.model.Campaign;
import com.campaignpro.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTaskService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private AnalyticsService analyticsService;

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        List<Campaign> scheduledCampaigns = campaignRepository.findByStatusAndScheduledTimeBefore(
                Campaign.CampaignStatus.SCHEDULED, now);

        for (Campaign campaign : scheduledCampaigns) {
            try {
                // Reload campaign to ensure we have the latest state
                Campaign currentCampaign = campaignRepository.findById(campaign.getId()).orElse(campaign);
                if (currentCampaign.getStatus() == Campaign.CampaignStatus.SCHEDULED) {
                    campaignService.sendCampaign(currentCampaign.getId(), currentCampaign.getUserId());
                }
            } catch (Exception e) {
                campaign.setStatus(Campaign.CampaignStatus.FAILED);
                campaignRepository.save(campaign);
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void updateAnalytics() {
        List<Campaign> sentCampaigns = campaignRepository.findByStatus(Campaign.CampaignStatus.SENT);

        for (Campaign campaign : sentCampaigns) {
            try {
                analyticsService.saveAnalytics(campaign.getId(), campaign.getUserId());
            } catch (Exception e) {
                // Log error but continue processing other campaigns
            }
        }
    }
}
