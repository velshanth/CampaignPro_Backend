package com.campaignpro.service;

import com.campaignpro.exception.BadRequestException;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.Contact;
import com.campaignpro.model.EmailDelivery;
import com.campaignpro.repository.ContactRepository;
import com.campaignpro.repository.EmailDeliveryRepository;
import com.campaignpro.repository.EmailTemplateRepository;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${email.from:noreply@campaignpro-backend-3dsw.onrender.com}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Autowired
    private EmailDeliveryRepository emailDeliveryRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EmailTemplateRepository templateRepository;

    @Autowired
    private TemplateService templateService;

    private Resend resend;

    @PostConstruct
    public void init() {
        this.resend = new Resend(resendApiKey);
    }

    public void sendCampaignEmails(Campaign campaign) {
        java.util.Set<String> processedEmails = new java.util.HashSet<>();
        List<String> errors = new ArrayList<>();

        if (campaign.getContactIds() != null) {
            for (String contactId : campaign.getContactIds()) {
                Contact contact = contactRepository.findById(contactId).orElse(null);
                if (contact != null && contact.isActive() && !processedEmails.contains(contact.getEmail())) {
                    sendEmailToContact(campaign, contact, errors);
                    processedEmails.add(contact.getEmail());
                }
            }
        }

        if (campaign.getGroupIds() != null) {
            for (String groupName : campaign.getGroupIds()) {
                List<Contact> groupContacts = contactRepository.findByUserIdAndGroupsContaining(campaign.getUserId(), groupName);
                for (Contact contact : groupContacts) {
                    if (contact.isActive() && !processedEmails.contains(contact.getEmail())) {
                        sendEmailToContact(campaign, contact, errors);
                        processedEmails.add(contact.getEmail());
                    }
                }
            }
        }

        if (processedEmails.isEmpty()) {
            throw new BadRequestException("No active recipients found for this campaign");
        }

        if (campaign.getSentCount() == 0) {
            throw new BadRequestException(errors.isEmpty()
                    ? "Failed to send campaign emails"
                    : errors.get(0));
        }
    }

    public void sendEmailToContact(Campaign campaign, Contact contact) {
        sendEmailToContact(campaign, contact, null);
    }

    private void sendEmailToContact(Campaign campaign, Contact contact, List<String> errors) {
        try {
            EmailDelivery delivery = new EmailDelivery(campaign.getId(), contact.getId(), contact.getEmail());
            delivery.setStatus(EmailDelivery.DeliveryStatus.PENDING);
            EmailDelivery savedDelivery = emailDeliveryRepository.save(delivery);

            Map<String, String> variables = new HashMap<>();
            variables.put("firstName", contact.getFirstName() != null ? contact.getFirstName() : "");
            variables.put("lastName", contact.getLastName() != null ? contact.getLastName() : "");
            variables.put("email", contact.getEmail());

            String trackingPixel = "<img src=\"" + appUrl + "/api/activities/track?campaignId=" + campaign.getId() +
                    "&deliveryId=" + savedDelivery.getId() + "&type=open\" width=\"1\" height=\"1\" />";

            String processedBody = templateService.processTemplate(campaign.getBody(), variables);
            processedBody += trackingPixel;
            processedBody = addClickTracking(processedBody, campaign.getId(), savedDelivery.getId());

            String processedSubject = templateService.processTemplate(campaign.getSubject(), variables);

            sendHtmlEmail(contact.getEmail(), processedSubject, processedBody);

            savedDelivery.setStatus(EmailDelivery.DeliveryStatus.SENT);
            savedDelivery.setSentAt(LocalDateTime.now());
            emailDeliveryRepository.save(savedDelivery);

            campaign.setSentCount(campaign.getSentCount() + 1);

        } catch (Exception e) {
            EmailDelivery delivery = emailDeliveryRepository.findByCampaignIdAndContactId(campaign.getId(), contact.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            String errorMessage = extractErrorMessage(e);

            if (delivery != null) {
                delivery.setStatus(EmailDelivery.DeliveryStatus.FAILED);
                delivery.setErrorMessage(errorMessage);
                delivery.setRetryCount(delivery.getRetryCount() + 1);
                emailDeliveryRepository.save(delivery);
            }

            campaign.setFailedCount(campaign.getFailedCount() + 1);
            if (errors != null) {
                errors.add(errorMessage);
            }

            if (isFatalProviderError(e)) {
                throw new BadRequestException(errorMessage, e);
            }
        }
    }

    private String extractErrorMessage(Throwable e) {
        ResendException resendException = findResendException(e);
        if (resendException != null && resendException.getMessage() != null) {
            return resendException.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "Failed to send email";
    }

    private boolean isFatalProviderError(Throwable e) {
        ResendException resendException = findResendException(e);
        if (resendException == null) {
            return false;
        }
        int statusCode = resendException.getStatusCode();
        return statusCode == 401 || statusCode == 403 || statusCode == 422;
    }

    private ResendException findResendException(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ResendException resendException) {
                return resendException;
            }
            current = current.getCause();
        }
        return null;
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);

            if (response.getId() == null) {
                throw new RuntimeException("Resend API error: Email not sent");
            }

        } catch (ResendException e) {
            throw new RuntimeException("Failed to send email via Resend", e);
        }
    }

    private String addClickTracking(String htmlContent, String campaignId, String deliveryId) {
        // Simple implementation - wrap href attributes with tracking URL
        String trackingUrl = appUrl + "/api/activities/track?campaignId=" + campaignId +
                "&deliveryId=" + deliveryId + "&type=click&url=";

        return htmlContent.replaceAll("href=\"([^\"]+)\"", "href=\"" + trackingUrl + "$1\"");
    }

    public void retryFailedEmails(String campaignId) {
        List<EmailDelivery> failedDeliveries = emailDeliveryRepository.findByCampaignIdAndStatus(
                campaignId, EmailDelivery.DeliveryStatus.FAILED);

        for (EmailDelivery delivery : failedDeliveries) {
            if (delivery.getRetryCount() < 3) { // Max 3 retries
                Contact contact = contactRepository.findById(delivery.getContactId()).orElse(null);
                if (contact != null) {
                    try {
                        Campaign campaign = new Campaign();
                        campaign.setId(campaignId);
                        // Reload campaign data if needed
                        sendEmailToContact(campaign, contact);
                    } catch (Exception e) {
                        delivery.setRetryCount(delivery.getRetryCount() + 1);
                        emailDeliveryRepository.save(delivery);
                    }
                }
            }
        }
    }

    public List<EmailDelivery> getEmailDeliveries(String campaignId) {
        return emailDeliveryRepository.findByCampaignId(campaignId);
    }

    public List<EmailDelivery> getBouncedEmails() {
        return emailDeliveryRepository.findByStatus(EmailDelivery.DeliveryStatus.BOUNCED);
    }
}
