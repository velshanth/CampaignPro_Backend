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
@Document(collection = "analytics")
public class Analytics {

    @Id
    private String id;

    @Indexed
    private String campaignId;

    @Indexed
    private String userId;

    private int totalSent = 0;

    private int totalDelivered = 0;

    private int totalOpened = 0;

    private int totalClicked = 0;

    private int totalBounced = 0;

    private double deliveryRate = 0.0;

    private double openRate = 0.0;

    private double clickRate = 0.0;

    private LocalDateTime calculatedAt;

    public Analytics(String campaignId, String userId) {
        this.campaignId = campaignId;
        this.userId = userId;
        this.calculatedAt = LocalDateTime.now();
    }
}
