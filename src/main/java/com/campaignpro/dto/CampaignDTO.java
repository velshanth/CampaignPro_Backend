package com.campaignpro.dto;

import com.campaignpro.model.Campaign;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDTO {

    private String id;
    private String userId;

    @NotBlank(message = "Campaign name is required")
    private String name;

    @NotNull(message = "Template ID is required")
    private String templateId;

    private List<String> contactIds;
    private List<String> groupIds;
    private Campaign.CampaignStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime sentTime;
    private int totalRecipients;
    private int sentCount;
    private int failedCount;
    private String subject;
    private String body;

    public CampaignDTO(String name, String templateId) {
        this.name = name;
        this.templateId = templateId;
    }
}
