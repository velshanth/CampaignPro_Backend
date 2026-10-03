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
@Document(collection = "contacts")
public class Contact {

    @Id
    private String id;

    @Indexed
    private String userId; // Reference to user who owns this contact

    private String firstName;

    private String lastName;

    @Indexed
    private String email;

    private String phone;

    private List<String> groups = new ArrayList<>(); // List of group names

    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Contact(String userId, String firstName, String lastName, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
