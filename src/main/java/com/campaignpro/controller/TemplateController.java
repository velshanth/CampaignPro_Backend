package com.campaignpro.controller;

import com.campaignpro.dto.TemplateDTO;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserRepository userRepository;

    private String getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.campaignpro.exception.ResourceNotFoundException("User", "username", username));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<TemplateDTO>> getAllTemplates(Authentication authentication) {
        String userId = getUserId(authentication);
        List<TemplateDTO> templates = templateService.getAllTemplates(userId);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDTO> getTemplateById(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        TemplateDTO template = templateService.getTemplateById(id, userId);
        return ResponseEntity.ok(template);
    }

    @PostMapping
    public ResponseEntity<TemplateDTO> createTemplate(@Valid @RequestBody TemplateDTO templateDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        TemplateDTO createdTemplate = templateService.createTemplate(userId, templateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateDTO> updateTemplate(@PathVariable String id, @Valid @RequestBody TemplateDTO templateDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        TemplateDTO updatedTemplate = templateService.updateTemplate(id, userId, templateDTO);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        templateService.deleteTemplate(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<String> previewTemplate(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        String preview = templateService.previewTemplate(id, userId);
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/default")
    public ResponseEntity<List<TemplateDTO>> getDefaultTemplates() {
        List<TemplateDTO> templates = templateService.getDefaultTemplates();
        return ResponseEntity.ok(templates);
    }
}
