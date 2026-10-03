package com.campaignpro.service;

import com.campaignpro.dto.AnalyticsDTO;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.User;
import com.campaignpro.repository.CampaignRepository;
import com.campaignpro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private AnalyticsService analyticsService;

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();

        // Campaign statistics
        List<Campaign> allCampaigns = campaignRepository.findAll();
        long totalCampaigns = allCampaigns.size();
        long sentCampaigns = allCampaigns.stream()
                .filter(c -> c.getStatus() == Campaign.CampaignStatus.SENT).count();
        long scheduledCampaigns = allCampaigns.stream()
                .filter(c -> c.getStatus() == Campaign.CampaignStatus.SCHEDULED).count();

        dashboard.put("totalUsers", totalUsers);
        dashboard.put("activeUsers", activeUsers);
        dashboard.put("totalCampaigns", totalCampaigns);
        dashboard.put("sentCampaigns", sentCampaigns);
        dashboard.put("scheduledCampaigns", scheduledCampaigns);

        return dashboard;
    }

    public List<Map<String, Object>> getAllUsersWithStats() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("id", user.getId());
            userStats.put("username", user.getUsername());
            userStats.put("email", user.getEmail());
            userStats.put("fullName", user.getFullName());
            userStats.put("role", user.getRole());
            userStats.put("active", user.isActive());

            // Add campaign count for each user
            long campaignCount = campaignRepository.findByUserId(user.getId()).size();
            userStats.put("campaignCount", campaignCount);

            return userStats;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllCampaignsWithStats() {
        List<Campaign> campaigns = campaignRepository.findAll();
        return campaigns.stream().map(campaign -> {
            Map<String, Object> campaignStats = new HashMap<>();
            campaignStats.put("id", campaign.getId());
            campaignStats.put("name", campaign.getName());
            campaignStats.put("userId", campaign.getUserId());
            campaignStats.put("status", campaign.getStatus());
            campaignStats.put("totalRecipients", campaign.getTotalRecipients());
            campaignStats.put("sentCount", campaign.getSentCount());
            campaignStats.put("failedCount", campaign.getFailedCount());
            campaignStats.put("createdAt", campaign.getCreatedAt());
            campaignStats.put("sentTime", campaign.getSentTime());

            // Get user name
            User user = userRepository.findById(campaign.getUserId()).orElse(null);
            campaignStats.put("userName", user != null ? user.getFullName() : "Unknown");

            return campaignStats;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSystemActivities() {
        // This would typically come from an activity log
        // For now, return recent campaigns as activities
        List<Campaign> recentCampaigns = campaignRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .limit(20)
                .collect(Collectors.toList());

        return recentCampaigns.stream().map(campaign -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "CAMPAIGN");
            activity.put("description", "Campaign " + campaign.getName() + " was " + campaign.getStatus());
            activity.put("userId", campaign.getUserId());
            activity.put("timestamp", campaign.getCreatedAt());

            User user = userRepository.findById(campaign.getUserId()).orElse(null);
            activity.put("userName", user != null ? user.getFullName() : "Unknown");

            return activity;
        }).collect(Collectors.toList());
    }

    public AnalyticsDTO getPlatformAnalytics() {
        List<Campaign> allCampaigns = campaignRepository.findAll();

        int totalSent = allCampaigns.stream().mapToInt(Campaign::getSentCount).sum();
        int totalRecipients = allCampaigns.stream().mapToInt(Campaign::getTotalRecipients).sum();
        int totalFailed = allCampaigns.stream().mapToInt(Campaign::getFailedCount).sum();

        AnalyticsDTO platformAnalytics = new AnalyticsDTO();
        platformAnalytics.setTotalSent(totalSent);
        platformAnalytics.setTotalDelivered(totalSent - totalFailed);
        platformAnalytics.setTotalBounced(totalFailed);

        if (totalSent > 0) {
            platformAnalytics.setDeliveryRate((double) (totalSent - totalFailed) / totalSent * 100);
        }

        // Calculate total opens and clicks across all campaigns
        int totalOpened = 0;
        int totalClicked = 0;

        for (Campaign campaign : allCampaigns) {
            try {
                AnalyticsDTO campaignAnalytics = analyticsService.calculateCampaignAnalytics(campaign.getId());
                totalOpened += campaignAnalytics.getTotalOpened();
                totalClicked += campaignAnalytics.getTotalClicked();
            } catch (Exception e) {
                // Skip campaigns that can't be analyzed
            }
        }

        platformAnalytics.setTotalOpened(totalOpened);
        platformAnalytics.setTotalClicked(totalClicked);

        if (platformAnalytics.getTotalDelivered() > 0) {
            platformAnalytics.setOpenRate((double) totalOpened / platformAnalytics.getTotalDelivered() * 100);
        }
        if (totalOpened > 0) {
            platformAnalytics.setClickRate((double) totalClicked / totalOpened * 100);
        }

        return platformAnalytics;
    }

    public List<Map<String, Object>> getSystemLogs() {
        // This would typically come from a logging system
        // For now, return a placeholder
        return List.of(
                Map.of("level", "INFO", "message", "System started", "timestamp", LocalDateTime.now()),
                Map.of("level", "INFO", "message", "Database connected", "timestamp", LocalDateTime.now()),
                Map.of("level", "INFO", "message", "Email service initialized", "timestamp", LocalDateTime.now())
        );
    }
}
