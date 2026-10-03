package com.campaignpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {

    private String id;
    private String campaignId;
    private String userId;
    private int totalSent;
    private int totalDelivered;
    private int totalOpened;
    private int totalClicked;
    private int totalBounced;
    private double deliveryRate;
    private double openRate;
    private double clickRate;
}
