package com.campaignpro.controller;

import com.campaignpro.model.EmailDelivery;
import com.campaignpro.repository.EmailDeliveryRepository;
import com.campaignpro.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailDeliveryRepository emailDeliveryRepository;

    @GetMapping("/deliveries")
    public ResponseEntity<List<EmailDelivery>> getEmailDeliveries(@RequestParam String campaignId) {
        List<EmailDelivery> deliveries = emailService.getEmailDeliveries(campaignId);
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<EmailDelivery> getDeliveryStatus(@PathVariable String id) {
        EmailDelivery delivery = emailDeliveryRepository.findById(id)
                .orElseThrow(() -> new com.campaignpro.exception.ResourceNotFoundException("EmailDelivery", "id", id));
        return ResponseEntity.ok(delivery);
    }

    @PostMapping("/retry")
    public ResponseEntity<Void> retryFailedEmails(@RequestParam String campaignId) {
        emailService.retryFailedEmails(campaignId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/bounces")
    public ResponseEntity<List<EmailDelivery>> getBouncedEmails() {
        List<EmailDelivery> bouncedEmails = emailService.getBouncedEmails();
        return ResponseEntity.ok(bouncedEmails);
    }
}
