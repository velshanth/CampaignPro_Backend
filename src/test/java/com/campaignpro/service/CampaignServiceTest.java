package com.campaignpro.service;

import com.campaignpro.dto.CampaignDTO;
import com.campaignpro.exception.BadRequestException;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.EmailTemplate;
import com.campaignpro.repository.CampaignRepository;
import com.campaignpro.repository.EmailTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private EmailTemplateRepository templateRepository;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign testCampaign;
    private EmailTemplate testTemplate;
    private CampaignDTO campaignDTO;

    @BeforeEach
    void setUp() {
        testTemplate = new EmailTemplate();
        testTemplate.setId("template1");
        testTemplate.setUserId("user1");
        testTemplate.setName("Test Template");
        testTemplate.setSubject("Test Subject");
        testTemplate.setBody("Test Body");
        testTemplate.setDefault(true);

        testCampaign = new Campaign();
        testCampaign.setId("campaign1");
        testCampaign.setUserId("user1");
        testCampaign.setName("Test Campaign");
        testCampaign.setTemplateId("template1");
        testCampaign.setStatus(Campaign.CampaignStatus.DRAFT);

        campaignDTO = new CampaignDTO();
        campaignDTO.setName("New Campaign");
        campaignDTO.setTemplateId("template1");
    }

    @Test
    void testCreateCampaignSuccess() {
        when(templateRepository.findById("template1")).thenReturn(Optional.of(testTemplate));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        CampaignDTO result = campaignService.createCampaign("user1", campaignDTO);

        assertNotNull(result);
        assertEquals("Test Campaign", result.getName());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void testCreateCampaignTemplateNotFound() {
        when(templateRepository.findById("template1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            campaignService.createCampaign("user1", campaignDTO);
        });
    }

    @Test
    void testCreateCampaignWithInvalidTemplateAccess() {
        testTemplate.setUserId("otherUser");
        testTemplate.setDefault(false);

        when(templateRepository.findById("template1")).thenReturn(Optional.of(testTemplate));

        assertThrows(BadRequestException.class, () -> {
            campaignService.createCampaign("user1", campaignDTO);
        });
    }

    @Test
    void testGetCampaignByIdSuccess() {
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));

        CampaignDTO result = campaignService.getCampaignById("campaign1", "user1");

        assertNotNull(result);
        assertEquals("Test Campaign", result.getName());
    }

    @Test
    void testGetCampaignByIdWrongUser() {
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));

        assertThrows(BadRequestException.class, () -> {
            campaignService.getCampaignById("campaign1", "otherUser");
        });
    }

    @Test
    void testDeleteCampaignSuccess() {
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));

        campaignService.deleteCampaign("campaign1", "user1");

        verify(campaignRepository, times(1)).delete(testCampaign);
    }

    @Test
    void testDeleteCampaignAlreadySent() {
        testCampaign.setStatus(Campaign.CampaignStatus.SENT);
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));

        assertThrows(BadRequestException.class, () -> {
            campaignService.deleteCampaign("campaign1", "user1");
        });
    }

    @Test
    void testScheduleCampaignSuccess() {
        LocalDateTime scheduledTime = LocalDateTime.now().plusDays(1);
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.scheduleCampaign("campaign1", "user1", scheduledTime);

        assertEquals(Campaign.CampaignStatus.SCHEDULED, testCampaign.getStatus());
        assertEquals(scheduledTime, testCampaign.getScheduledTime());
    }

    @Test
    void testScheduleCampaignPastTime() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        when(campaignRepository.findById("campaign1")).thenReturn(Optional.of(testCampaign));

        assertThrows(BadRequestException.class, () -> {
            campaignService.scheduleCampaign("campaign1", "user1", pastTime);
        });
    }
}
