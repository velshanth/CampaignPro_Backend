package com.campaignpro.repository;

import com.campaignpro.model.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {
    List<Campaign> findByUserId(String userId);
    List<Campaign> findByUserIdAndStatus(String userId, Campaign.CampaignStatus status);
    List<Campaign> findByStatusAndScheduledTimeBefore(Campaign.CampaignStatus status, LocalDateTime time);
    List<Campaign> findByStatus(Campaign.CampaignStatus status);
}
