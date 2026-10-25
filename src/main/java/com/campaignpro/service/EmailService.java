package com.campaignpro.service;

import com.campaignpro.exception.BadRequestException;
import com.campaignpro.model.Campaign;
import com.campaignpro.model.Contact;
import com.campaignpro.model.EmailDelivery;
import com.campaignpro.repository.ContactRepository;
import com.campaignpro.repository.EmailDeliveryRepository;
import com.campaignpro.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${email.from:your-email@gmail.com}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailDeliveryRepository emailDeliveryRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EmailTemplateRepository templateRepository;

    @Autowired
    private TemplateService templateService;

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
        return e.getMessage() != null ? e.getMessage() : "Failed to send email";
    }

    private boolean isFatalProviderError(Throwable e) {
        if (e instanceof MessagingException) {
            MessagingException me = (MessagingException) e;
            return me.getMessage() != null && 
                   (me.getMessage().contains("Authentication failed") ||
                    me.getMessage().contains("Invalid credentials") ||
                    me.getMessage().contains("Access denied"));
        }
        return false;
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email via Java Mail API", e);
        }
    }

    private String addClickTracking(String htmlContent, String campaignId, String deliveryId) {
        String trackingUrl = appUrl + "/api/activities/track?campaignId=" + campaignId +
                "&deliveryId=" + deliveryId + "&type=click&url=";

        return htmlContent.replaceAll("href=\"([^\"]+)\"", "href=\"" + trackingUrl + "$1\"");
    }

    public void retryFailedEmails(String campaignId) {
        List<EmailDelivery> failedDeliveries = emailDeliveryRepository.findByCampaignIdAndStatus(
                campaignId, EmailDelivery.DeliveryStatus.FAILED);

        for (EmailDelivery delivery : failedDeliveries) {
            if (delivery.getRetryCount() < 3) {
                Contact contact = contactRepository.findById(delivery.getContactId()).orElse(null);
                if (contact != null) {
                    try {
                        Campaign campaign = new Campaign();
                        campaign.setId(campaignId);
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
