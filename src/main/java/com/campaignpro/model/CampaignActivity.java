package com.campaignpro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campaign_activities")
public class CampaignActivity {

    @Id
    private String id;

    @Indexed
    private String campaignId;

    @Indexed
    private String deliveryId;

    @Indexed
    private String contactId;

    private ActivityType type; // OPEN, CLICK, BOUNCE, UNSUBSCRIBE

    private String linkClicked; // For click activities

    private String userAgent;

    private String ipAddress;

    private LocalDateTime timestamp;

    public enum ActivityType {
        OPEN, CLICK, BOUNCE, UNSUBSCRIBE
    }

    public CampaignActivity(String campaignId, String deliveryId, String contactId, ActivityType type) {
        this.campaignId = campaignId;
        this.deliveryId = deliveryId;
        this.contactId = contactId;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
}
