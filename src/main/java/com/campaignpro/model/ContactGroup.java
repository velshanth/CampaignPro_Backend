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
@Document(collection = "contact_groups")
public class ContactGroup {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;

    private String description;

    private int contactCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ContactGroup(String userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
