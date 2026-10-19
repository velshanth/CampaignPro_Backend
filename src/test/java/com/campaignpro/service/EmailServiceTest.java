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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailDeliveryRepository emailDeliveryRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private EmailTemplateRepository templateRepository;

    @Mock
    private TemplateService templateService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private Campaign testCampaign;
    private Contact testContact;
    private EmailDelivery testDelivery;

    @BeforeEach
    void setUp() {
        testCampaign = new Campaign();
        testCampaign.setId("campaign1");
        testCampaign.setUserId("user1");
        testCampaign.setName("Test Campaign");
        testCampaign.setSubject("Test Subject");
        testCampaign.setBody("Test Body");
        testCampaign.setContactIds(Arrays.asList("contact1"));

        testContact = new Contact();
        testContact.setId("contact1");
        testContact.setUserId("user1");
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setEmail("john@example.com");
        testContact.setActive(true);

        testDelivery = new EmailDelivery();
        testDelivery.setId("delivery1");
        testDelivery.setCampaignId("campaign1");
        testDelivery.setContactId("contact1");
        testDelivery.setEmail("john@example.com");
        testDelivery.setStatus(EmailDelivery.DeliveryStatus.PENDING);

        try {
            java.lang.reflect.Field fromEmailField = EmailService.class.getDeclaredField("fromEmail");
            fromEmailField.setAccessible(true);
            fromEmailField.set(emailService, "test@example.com");

            java.lang.reflect.Field appUrlField = EmailService.class.getDeclaredField("appUrl");
            appUrlField.setAccessible(true);
            appUrlField.set(emailService, "http://localhost:8080");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSendHtmlEmailSuccess() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> {
            emailService.sendHtmlEmail("test@example.com", "Test Subject", "<p>Test Body</p>");
        });

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlEmailFailure() {
        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("Connection failed"));

        assertThrows(RuntimeException.class, () ->
                emailService.sendHtmlEmail(
                        "test@example.com",
                        "Test Subject",
                        "<p>Test Body</p>"
                )
        );
    }

    @Test
    void testSendCampaignEmailsSuccess() {
        when(contactRepository.findById("contact1")).thenReturn(Optional.of(testContact));
        when(emailDeliveryRepository.save(any(EmailDelivery.class))).thenReturn(testDelivery);
        when(templateService.processTemplate(anyString(), any())).thenReturn("Processed Body");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> {
            emailService.sendCampaignEmails(testCampaign);
        });

        verify(emailDeliveryRepository, atLeastOnce()).save(any(EmailDelivery.class));
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void testSendCampaignEmailsNoRecipients() {
        testCampaign.setContactIds(null);
        testCampaign.setGroupIds(null);

        assertThrows(BadRequestException.class, () -> {
            emailService.sendCampaignEmails(testCampaign);
        });
    }

    @Test
    void testRetryFailedEmailsSuccess() {
        testDelivery.setStatus(EmailDelivery.DeliveryStatus.FAILED);
        testDelivery.setRetryCount(0);

        when(emailDeliveryRepository.findByCampaignIdAndStatus("campaign1", EmailDelivery.DeliveryStatus.FAILED))
                .thenReturn(Arrays.asList(testDelivery));
        when(contactRepository.findById("contact1")).thenReturn(Optional.of(testContact));
        when(emailDeliveryRepository.save(any(EmailDelivery.class))).thenReturn(testDelivery);

        emailService.retryFailedEmails("campaign1");

        verify(emailDeliveryRepository, atLeastOnce()).save(any(EmailDelivery.class));
    }

    @Test
    void testGetEmailDeliveriesSuccess() {
        when(emailDeliveryRepository.findByCampaignId("campaign1")).thenReturn(Arrays.asList(testDelivery));

        var result = emailService.getEmailDeliveries("campaign1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetBouncedEmailsSuccess() {
        testDelivery.setStatus(EmailDelivery.DeliveryStatus.BOUNCED);
        when(emailDeliveryRepository.findByStatus(EmailDelivery.DeliveryStatus.BOUNCED))
                .thenReturn(Arrays.asList(testDelivery));

        var result = emailService.getBouncedEmails();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
