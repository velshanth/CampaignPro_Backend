package com.campaignpro.service;

import com.campaignpro.dto.AnalyticsDTO;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.Analytics;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.CampaignActivity;
import com.campaignpro.model.EmailDelivery;
import com.campaignpro.repository.AnalyticsRepository;
import com.campaignpro.repository.CampaignActivityRepository;
import com.campaignpro.repository.CampaignRepository;
import com.campaignpro.repository.EmailDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignActivityRepository activityRepository;

    @Autowired
    private EmailDeliveryRepository emailDeliveryRepository;

    public AnalyticsDTO getOverviewAnalytics(String userId) {
        List<Campaign> userCampaigns = campaignRepository.findByUserId(userId);

        int totalSent = 0;
        int totalDelivered = 0;
        int totalOpened = 0;
        int totalClicked = 0;
        int totalBounced = 0;

        for (Campaign campaign : userCampaigns) {
            totalSent += campaign.getSentCount();
            totalDelivered += emailDeliveryRepository.countByCampaignIdAndStatus(
                    campaign.getId(), EmailDelivery.DeliveryStatus.DELIVERED);
            totalOpened += activityRepository.countByCampaignIdAndType(
                    campaign.getId(), CampaignActivity.ActivityType.OPEN);
            totalClicked += activityRepository.countByCampaignIdAndType(
                    campaign.getId(), CampaignActivity.ActivityType.CLICK);
            totalBounced += emailDeliveryRepository.countByCampaignIdAndStatus(
                    campaign.getId(), EmailDelivery.DeliveryStatus.BOUNCED);
        }

        AnalyticsDTO overview = new AnalyticsDTO();
        overview.setUserId(userId);
        overview.setTotalSent(totalSent);
        overview.setTotalDelivered(totalDelivered);
        overview.setTotalOpened(totalOpened);
        overview.setTotalClicked(totalClicked);
        overview.setTotalBounced(totalBounced);

        if (totalSent > 0) {
            overview.setDeliveryRate((double) totalDelivered / totalSent * 100);
            overview.setOpenRate((double) totalOpened / totalDelivered * 100);
            overview.setClickRate((double) totalClicked / totalOpened * 100);
        }

        return overview;
    }

    public List<AnalyticsDTO> getCampaignAnalytics(String userId) {
        List<Campaign> campaigns = campaignRepository.findByUserId(userId);
        return campaigns.stream()
                .map(campaign -> calculateCampaignAnalytics(campaign.getId()))
                .collect(Collectors.toList());
    }

    public AnalyticsDTO getCampaignAnalytics(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Campaign", "id", campaignId);
        }

        return calculateCampaignAnalytics(campaignId);
    }

    public AnalyticsDTO calculateCampaignAnalytics(String campaignId) {
        int totalSent = emailDeliveryRepository.countByCampaignId(campaignId);
        int totalDelivered = emailDeliveryRepository.countByCampaignIdAndStatus(
                campaignId, EmailDelivery.DeliveryStatus.DELIVERED);
        int totalOpened = activityRepository.countByCampaignIdAndType(
                campaignId, CampaignActivity.ActivityType.OPEN);
        int totalClicked = activityRepository.countByCampaignIdAndType(
                campaignId, CampaignActivity.ActivityType.CLICK);
        int totalBounced = emailDeliveryRepository.countByCampaignIdAndStatus(
                campaignId, EmailDelivery.DeliveryStatus.BOUNCED);

        AnalyticsDTO analytics = new AnalyticsDTO();
        analytics.setCampaignId(campaignId);
        analytics.setTotalSent(totalSent);
        analytics.setTotalDelivered(totalDelivered);
        analytics.setTotalOpened(totalOpened);
        analytics.setTotalClicked(totalClicked);
        analytics.setTotalBounced(totalBounced);

        if (totalSent > 0) {
            analytics.setDeliveryRate((double) totalDelivered / totalSent * 100);
        }
        if (totalDelivered > 0) {
            analytics.setOpenRate((double) totalOpened / totalDelivered * 100);
        }
        if (totalOpened > 0) {
            analytics.setClickRate((double) totalClicked / totalOpened * 100);
        }

        return analytics;
    }

    public List<CampaignActivity> getCampaignActivities(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Campaign", "id", campaignId);
        }

        return activityRepository.findByCampaignId(campaignId);
    }

    public List<CampaignActivity> getEmailOpens(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Campaign", "id", campaignId);
        }

        return activityRepository.findByCampaignIdAndType(campaignId, CampaignActivity.ActivityType.OPEN);
    }

    public List<CampaignActivity> getLinkClicks(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Campaign", "id", campaignId);
        }

        return activityRepository.findByCampaignIdAndType(campaignId, CampaignActivity.ActivityType.CLICK);
    }

    public void trackActivity(String campaignId, String deliveryId, String type, String additionalData) {
        CampaignActivity.ActivityType activityType;
        try {
            activityType = CampaignActivity.ActivityType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return; // Invalid activity type
        }

        EmailDelivery delivery = emailDeliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || !delivery.getCampaignId().equals(campaignId)) {
            return;
        }

        CampaignActivity activity = new CampaignActivity(campaignId, deliveryId, delivery.getContactId(), activityType);

        if (activityType == CampaignActivity.ActivityType.CLICK && additionalData != null) {
            activity.setLinkClicked(additionalData);
        }

        activityRepository.save(activity);

        // Update delivery status if opened
        if (activityType == CampaignActivity.ActivityType.OPEN) {
            delivery.setStatus(EmailDelivery.DeliveryStatus.DELIVERED);
            delivery.setDeliveredAt(LocalDateTime.now());
            emailDeliveryRepository.save(delivery);
        }
    }

    public void saveAnalytics(String campaignId, String userId) {
        AnalyticsDTO analytics = calculateCampaignAnalytics(campaignId);

        Analytics existingAnalytics = analyticsRepository.findByCampaignId(campaignId).orElse(null);
        if (existingAnalytics != null) {
            existingAnalytics.setTotalSent(analytics.getTotalSent());
            existingAnalytics.setTotalDelivered(analytics.getTotalDelivered());
            existingAnalytics.setTotalOpened(analytics.getTotalOpened());
            existingAnalytics.setTotalClicked(analytics.getTotalClicked());
            existingAnalytics.setTotalBounced(analytics.getTotalBounced());
            existingAnalytics.setDeliveryRate(analytics.getDeliveryRate());
            existingAnalytics.setOpenRate(analytics.getOpenRate());
            existingAnalytics.setClickRate(analytics.getClickRate());
            existingAnalytics.setCalculatedAt(LocalDateTime.now());
            analyticsRepository.save(existingAnalytics);
        } else {
            Analytics newAnalytics = new Analytics(campaignId, userId);
            newAnalytics.setTotalSent(analytics.getTotalSent());
            newAnalytics.setTotalDelivered(analytics.getTotalDelivered());
            newAnalytics.setTotalOpened(analytics.getTotalOpened());
            newAnalytics.setTotalClicked(analytics.getTotalClicked());
            newAnalytics.setTotalBounced(analytics.getTotalBounced());
            newAnalytics.setDeliveryRate(analytics.getDeliveryRate());
            newAnalytics.setOpenRate(analytics.getOpenRate());
            newAnalytics.setClickRate(analytics.getClickRate());
            analyticsRepository.save(newAnalytics);
        }
    }
}
