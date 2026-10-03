package com.campaignpro.service;

import com.campaignpro.dto.CampaignDTO;
import com.campaignpro.exception.BadRequestException;
import com.campaignpro.exception.ResourceNotFoundException;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.Contact;
import com.campaignpro.model.EmailTemplate;
import com.campaignpro.repository.CampaignRepository;
import com.campaignpro.repository.ContactRepository;
import com.campaignpro.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EmailTemplateRepository templateRepository;

    @Autowired
    private EmailService emailService;

    public List<CampaignDTO> getAllCampaigns(String userId) {
        return campaignRepository.findByUserId(userId).stream()
                .map(this::convertToCampaignDTO)
                .collect(Collectors.toList());
    }

    public CampaignDTO getCampaignById(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to access this campaign");
        }

        return convertToCampaignDTO(campaign);
    }

    public CampaignDTO createCampaign(String userId, CampaignDTO campaignDTO) {
        // Validate template exists and belongs to user
        EmailTemplate template = templateRepository.findById(campaignDTO.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template", "id", campaignDTO.getTemplateId()));

        if (!template.getUserId().equals(userId) && !template.isDefault()) {
            throw new BadRequestException("You don't have permission to use this template");
        }

        Campaign campaign = new Campaign(userId, campaignDTO.getName(), campaignDTO.getTemplateId());
        campaign.setContactIds(campaignDTO.getContactIds());
        campaign.setGroupIds(campaignDTO.getGroupIds());
        campaign.setSubject(template.getSubject());
        campaign.setBody(template.getBody());

        // Calculate total recipients
        int totalRecipients = calculateTotalRecipients(userId, campaignDTO.getContactIds(), campaignDTO.getGroupIds());
        campaign.setTotalRecipients(totalRecipients);

        // Handle scheduling at creation time
        if (campaignDTO.getScheduledTime() != null) {
            if (campaignDTO.getScheduledTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Scheduled time must be in the future");
            }
            campaign.setScheduledTime(campaignDTO.getScheduledTime());
            campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        } else {
            campaign.setStatus(Campaign.CampaignStatus.DRAFT);
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        return convertToCampaignDTO(savedCampaign);
    }

    public CampaignDTO updateCampaign(String campaignId, String userId, CampaignDTO campaignDTO) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this campaign");
        }

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT || campaign.getStatus() == Campaign.CampaignStatus.FAILED) {
            throw new BadRequestException("Cannot update a sent or failed campaign");
        }

        if (campaignDTO.getName() != null) {
            campaign.setName(campaignDTO.getName());
        }
        if (campaignDTO.getTemplateId() != null) {
            EmailTemplate template = templateRepository.findById(campaignDTO.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template", "id", campaignDTO.getTemplateId()));

            if (!template.getUserId().equals(userId) && !template.isDefault()) {
                throw new BadRequestException("You don't have permission to use this template");
            }

            campaign.setTemplateId(campaignDTO.getTemplateId());
            campaign.setSubject(template.getSubject());
            campaign.setBody(template.getBody());
        }
        if (campaignDTO.getContactIds() != null) {
            campaign.setContactIds(campaignDTO.getContactIds());
        }
        if (campaignDTO.getGroupIds() != null) {
            campaign.setGroupIds(campaignDTO.getGroupIds());
        }

        // Recalculate total recipients
        int totalRecipients = calculateTotalRecipients(userId, campaign.getContactIds(), campaign.getGroupIds());
        campaign.setTotalRecipients(totalRecipients);

        Campaign updatedCampaign = campaignRepository.save(campaign);
        return convertToCampaignDTO(updatedCampaign);
    }

    public void deleteCampaign(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this campaign");
        }

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT) {
            throw new BadRequestException("Cannot delete a sent campaign");
        }

        campaignRepository.delete(campaign);
    }

    public void sendCampaign(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to send this campaign");
        }

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT) {
            throw new BadRequestException("Campaign has already been sent");
        }

        try {
            emailService.sendCampaignEmails(campaign);
            campaign.setStatus(Campaign.CampaignStatus.SENT);
            campaign.setSentTime(LocalDateTime.now());
            campaignRepository.save(campaign);
        } catch (BadRequestException e) {
            campaign.setStatus(Campaign.CampaignStatus.FAILED);
            campaignRepository.save(campaign);
            throw e;
        } catch (Exception e) {
            campaign.setStatus(Campaign.CampaignStatus.FAILED);
            campaignRepository.save(campaign);
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    public void scheduleCampaign(String campaignId, String userId, LocalDateTime scheduledTime) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to schedule this campaign");
        }

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Scheduled time must be in the future");
        }

        campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        campaign.setScheduledTime(scheduledTime);
        campaignRepository.save(campaign);
    }

    public void cancelScheduledCampaign(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", campaignId));

        if (!campaign.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to cancel this campaign");
        }

        if (campaign.getStatus() != Campaign.CampaignStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled campaigns can be cancelled");
        }

        campaign.setStatus(Campaign.CampaignStatus.DRAFT);
        campaign.setScheduledTime(null);
        campaignRepository.save(campaign);
    }

    public List<CampaignDTO> getCampaignHistory(String userId) {
        return campaignRepository.findByUserId(userId).stream()
                .filter(c -> c.getStatus() == Campaign.CampaignStatus.SENT || c.getStatus() == Campaign.CampaignStatus.FAILED)
                .map(this::convertToCampaignDTO)
                .collect(Collectors.toList());
    }

    private int calculateTotalRecipients(String userId, List<String> contactIds, List<String> groupIds) {
        java.util.Set<String> uniqueRecipients = new java.util.HashSet<>();

        // Add individual contacts
        if (contactIds != null) {
            for (String contactId : contactIds) {
                Contact contact = contactRepository.findById(contactId).orElse(null);
                if (contact != null && contact.getUserId().equals(userId) && contact.isActive()) {
                    uniqueRecipients.add(contact.getEmail());
                }
            }
        }

        // Add contacts from groups
        if (groupIds != null) {
            for (String groupName : groupIds) {
                List<Contact> groupContacts = contactRepository.findByUserIdAndGroupsContaining(userId, groupName);
                for (Contact contact : groupContacts) {
                    if (contact.isActive()) {
                        uniqueRecipients.add(contact.getEmail());
                    }
                }
            }
        }

        return uniqueRecipients.size();
    }

    private CampaignDTO convertToCampaignDTO(Campaign campaign) {
        CampaignDTO dto = new CampaignDTO();
        dto.setId(campaign.getId());
        dto.setUserId(campaign.getUserId());
        dto.setName(campaign.getName());
        dto.setTemplateId(campaign.getTemplateId());
        dto.setContactIds(campaign.getContactIds());
        dto.setGroupIds(campaign.getGroupIds());
        dto.setStatus(campaign.getStatus());
        dto.setScheduledTime(campaign.getScheduledTime());
        dto.setSentTime(campaign.getSentTime());
        dto.setTotalRecipients(campaign.getTotalRecipients());
        dto.setSentCount(campaign.getSentCount());
        dto.setFailedCount(campaign.getFailedCount());
        dto.setSubject(campaign.getSubject());
        dto.setBody(campaign.getBody());
        return dto;
    }
}
