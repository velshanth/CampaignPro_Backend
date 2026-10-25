package com.campaignpro.service;

import com.campaignpro.dto.TemplateDTO;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.EmailTemplate;
import com.campaignpro.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private EmailTemplateRepository templateRepository;

    @InjectMocks
    private TemplateService templateService;

    private EmailTemplate testTemplate;
    private TemplateDTO templateDTO;

    @BeforeEach
    void setUp() {
        testTemplate = new EmailTemplate();
        testTemplate.setId("template1");
        testTemplate.setUserId("user1");
        testTemplate.setName("Test Template");
        testTemplate.setSubject("Test Subject");
        testTemplate.setBody("Hello {{firstName}}");
        testTemplate.setDefault(false);

        templateDTO = new TemplateDTO();
        templateDTO.setName("New Template");
        templateDTO.setSubject("New Subject");
        templateDTO.setBody("New Body");
    }

    @Test
    void testCreateTemplateSuccess() {
        when(templateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);

        TemplateDTO result = templateService.createTemplate("user1", templateDTO);

        assertNotNull(result);
        assertEquals("Test Template", result.getName());
        verify(templateRepository, times(1)).save(any(EmailTemplate.class));
    }

    @Test
    void testGetAllTemplatesSuccess() {
        when(templateRepository.findByUserId("user1")).thenReturn(Arrays.asList(testTemplate));

        List<TemplateDTO> result = templateService.getAllTemplates("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Template", result.get(0).getName());
    }

    @Test
    void testGetTemplateByIdSuccess() {
        when(templateRepository.findById("template1")).thenReturn(Optional.of(testTemplate));

        TemplateDTO result = templateService.getTemplateById("template1", "user1");

        assertNotNull(result);
        assertEquals("Test Template", result.getName());
    }

    @Test
    void testGetTemplateByIdNotFound() {
        when(templateRepository.findById("template1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            templateService.getTemplateById("template1", "user1");
        });
    }

    @Test
    void testUpdateTemplateSuccess() {
        when(templateRepository.findById("template1")).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(EmailTemplate.class))).thenReturn(testTemplate);

        TemplateDTO result = templateService.updateTemplate("template1", "user1", templateDTO);

        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(EmailTemplate.class));
    }

    @Test
    void testDeleteTemplateSuccess() {
        when(templateRepository.findById("template1")).thenReturn(Optional.of(testTemplate));

        templateService.deleteTemplate("template1", "user1");

        verify(templateRepository, times(1)).delete(testTemplate);
    }

    @Test
    void testProcessTemplateSuccess() {
        java.util.Map<String, String> variables = new java.util.HashMap<>();
        variables.put("firstName", "John");

        String result = templateService.processTemplate("Hello {{firstName}}", variables);

        assertEquals("Hello John", result);
    }

    @Test
    void testProcessTemplateMultiplePlaceholders() {
        java.util.Map<String, String> variables = new java.util.HashMap<>();
        variables.put("firstName", "John");
        variables.put("lastName", "Doe");

        String result = templateService.processTemplate("Hello {{firstName}} {{lastName}}", variables);

        assertEquals("Hello John Doe", result);
    }
}
