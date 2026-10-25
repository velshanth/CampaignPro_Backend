package com.campaignpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroupDTO {

    private String id;
    private String userId;

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;
    private int contactCount;

    public ContactGroupDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
