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
@Document(collection = "email_deliveries")
public class EmailDelivery {

    @Id
    private String id;

    @Indexed
    private String campaignId;

    @Indexed
    private String contactId;

    private String email;

    private DeliveryStatus status = DeliveryStatus.PENDING; // PENDING, SENT, DELIVERED, FAILED, BOUNCED

    private String errorMessage;

    private int retryCount = 0;

    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt;

    public enum DeliveryStatus {
        PENDING, SENT, DELIVERED, FAILED, BOUNCED
    }

    public EmailDelivery(String campaignId, String contactId, String email) {
        this.campaignId = campaignId;
        this.contactId = contactId;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
}
