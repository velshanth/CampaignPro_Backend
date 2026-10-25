package com.campaignpro.repository;

import com.campaignpro.model.EmailDelivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailDeliveryRepository extends MongoRepository<EmailDelivery, String> {
    List<EmailDelivery> findByCampaignId(String campaignId);
    List<EmailDelivery> findByCampaignIdAndStatus(String campaignId, EmailDelivery.DeliveryStatus status);
    List<EmailDelivery> findByStatus(EmailDelivery.DeliveryStatus status);
    List<EmailDelivery> findByCampaignIdAndContactId(String campaignId, String contactId);
    int countByCampaignId(String campaignId);
    int countByCampaignIdAndStatus(String campaignId, EmailDelivery.DeliveryStatus status);
}
