package com.campaignpro.service;

import com.campaignpro.dto.TemplateDTO;
import com.campaignpro.exception.BadRequestException;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.EmailTemplate;
import com.campaignpro.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    @Autowired
    private EmailTemplateRepository templateRepository;

    public List<TemplateDTO> getAllTemplates(String userId) {
        return templateRepository.findByUserId(userId).stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
    }

    public TemplateDTO getTemplateById(String templateId, String userId) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", templateId));

        if (!template.getUserId().equals(userId) && !template.isDefault()) {
            throw new BadRequestException("You don't have permission to access this template");
        }

        return convertToTemplateDTO(template);
    }

    public TemplateDTO createTemplate(String userId, TemplateDTO templateDTO) {
        EmailTemplate template = new EmailTemplate(userId, templateDTO.getName(), templateDTO.getSubject(), templateDTO.getBody());
        template.setPlaceholders(extractPlaceholders(templateDTO.getBody()));
        EmailTemplate savedTemplate = templateRepository.save(template);
        return convertToTemplateDTO(savedTemplate);
    }

    public TemplateDTO updateTemplate(String templateId, String userId, TemplateDTO templateDTO) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", templateId));

        if (!template.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this template");
        }

        if (templateDTO.getName() != null) {
            template.setName(templateDTO.getName());
        }
        if (templateDTO.getSubject() != null) {
            template.setSubject(templateDTO.getSubject());
        }
        if (templateDTO.getBody() != null) {
            template.setBody(templateDTO.getBody());
            template.setPlaceholders(extractPlaceholders(templateDTO.getBody()));
        }

        EmailTemplate updatedTemplate = templateRepository.save(template);
        return convertToTemplateDTO(updatedTemplate);
    }

    public void deleteTemplate(String templateId, String userId) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", templateId));

        if (!template.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this template");
        }

        templateRepository.delete(template);
    }

    public String previewTemplate(String templateId, String userId) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", templateId));

        if (!template.getUserId().equals(userId) && !template.isDefault()) {
            throw new BadRequestException("You don't have permission to access this template");
        }

        return template.getBody();
    }

    public List<TemplateDTO> getDefaultTemplates() {
        return templateRepository.findByIsDefaultTrue().stream()
                .map(this::convertToTemplateDTO)
                .collect(Collectors.toList());
    }

    public String processTemplate(String templateContent, java.util.Map<String, String> variables) {
        String processedContent = templateContent;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processedContent = processedContent.replace(placeholder, entry.getValue());
        }
        return processedContent;
    }

    private List<String> extractPlaceholders(String content) {
        List<String> placeholders = new java.util.ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    private TemplateDTO convertToTemplateDTO(EmailTemplate template) {
        TemplateDTO dto = new TemplateDTO();
        dto.setId(template.getId());
        dto.setUserId(template.getUserId());
        dto.setName(template.getName());
        dto.setSubject(template.getSubject());
        dto.setBody(template.getBody());
        dto.setPlaceholders(template.getPlaceholders());
        dto.setDefault(template.isDefault());
        return dto;
    }
}
