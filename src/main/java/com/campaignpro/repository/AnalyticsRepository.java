package com.campaignpro.repository;

import com.campaignpro.model.Analytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends MongoRepository<Analytics, String> {
    List<Analytics> findByUserId(String userId);
    Optional<Analytics> findByCampaignId(String campaignId);
    List<Analytics> findByUserIdOrderByCalculatedAtDesc(String userId);
}
