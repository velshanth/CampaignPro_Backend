package com.campaignpro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {

    private String id;
    private String userId;

    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Email body is required")
    private String body;

    private List<String> placeholders;

    @JsonProperty("default")
    private boolean isDefault;

    public TemplateDTO(String name, String subject, String body) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }
}
