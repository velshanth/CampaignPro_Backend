package com.campaignpro.controller;

import com.campaignpro.dto.CampaignDTO;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.EmailTemplate;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
public class CampaignControllerTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignService campaignService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CampaignController campaignController;

    private User testUser;
    private Campaign testCampaign;
    private EmailTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole("USER");

        testTemplate = new EmailTemplate();
        testTemplate.setId("template1");
        testTemplate.setUserId("1");
        testTemplate.setName("Test Template");
        testTemplate.setSubject("Test Subject");
        testTemplate.setBody("Test Body");
        testTemplate.setDefault(true);

        testCampaign = new Campaign();
        testCampaign.setId("campaign1");
        testCampaign.setUserId("1");
        testCampaign.setName("Test Campaign");
        testCampaign.setTemplateId("template1");
        testCampaign.setStatus(Campaign.CampaignStatus.DRAFT);
        testCampaign.setCreatedAt(LocalDateTime.now());

        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testGetAllCampaigns() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(campaignService.getAllCampaigns("1")).thenReturn(Arrays.asList(new CampaignDTO()));

        var result = campaignController.getAllCampaigns(authentication);

        assertNotNull(result);
        assertEquals(OK, result.getStatusCode());
    }

    @Test
    void testGetCampaignById() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        CampaignDTO dto = new CampaignDTO();
        dto.setName("Test Campaign");
        when(campaignService.getCampaignById("campaign1", "1")).thenReturn(dto);

        var result = campaignController.getCampaignById("campaign1", authentication);

        assertNotNull(result);
        assertEquals(OK, result.getStatusCode());
        assertEquals("Test Campaign", result.getBody().getName());
    }

    @Test
    void testCreateCampaign() {
        CampaignDTO campaignDTO = new CampaignDTO();
        campaignDTO.setName("New Campaign");
        campaignDTO.setTemplateId("template1");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(campaignService.createCampaign("1", campaignDTO)).thenReturn(campaignDTO);

        var result = campaignController.createCampaign(campaignDTO, authentication);

        assertNotNull(result);
        assertEquals(CREATED, result.getStatusCode());
    }

    @Test
    void testDeleteCampaign() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(campaignService).deleteCampaign("campaign1", "1");

        var result = campaignController.deleteCampaign("campaign1", authentication);

        assertEquals(NO_CONTENT, result.getStatusCode());
    }
}
