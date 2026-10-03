package com.campaignpro.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campaigns")
public class Campaign {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;

    private String templateId;

    private List<String> contactIds = new ArrayList<>();

    private List<String> groupIds = new ArrayList<>();

    private CampaignStatus status = CampaignStatus.DRAFT; // DRAFT, SCHEDULED, SENT, FAILED

    private LocalDateTime scheduledTime;

    private LocalDateTime sentTime;

    private int totalRecipients = 0;

    private int sentCount = 0;

    private int failedCount = 0;

    private String subject;

    private String body;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum CampaignStatus {
        DRAFT, SCHEDULED, SENT, FAILED
    }

    public Campaign(String userId, String name, String templateId) {
        this.userId = userId;
        this.name = name;
        this.templateId = templateId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
