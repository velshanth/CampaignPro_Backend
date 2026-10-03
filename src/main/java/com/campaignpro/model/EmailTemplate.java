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
@Document(collection = "email_templates")
public class EmailTemplate {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;

    private String subject;

    private String body; // HTML content with placeholders

    private List<String> placeholders = new ArrayList<>(); // e.g., ["{{firstName}}", "{{lastName}}"]

    private boolean isDefault = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public EmailTemplate(String userId, String name, String subject, String body) {
        this.userId = userId;
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
