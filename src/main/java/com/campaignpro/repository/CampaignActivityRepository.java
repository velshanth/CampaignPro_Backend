package com.campaignpro.repository;

import com.campaignpro.model.CampaignActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignActivityRepository extends MongoRepository<CampaignActivity, String> {
    List<CampaignActivity> findByCampaignId(String campaignId);
    List<CampaignActivity> findByCampaignIdAndType(String campaignId, CampaignActivity.ActivityType type);
    List<CampaignActivity> findByDeliveryId(String deliveryId);
    int countByCampaignIdAndType(String campaignId, CampaignActivity.ActivityType type);
}
