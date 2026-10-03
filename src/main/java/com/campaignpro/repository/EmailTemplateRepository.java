package com.campaignpro.repository;

import com.campaignpro.model.EmailTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
    List<EmailTemplate> findByUserId(String userId);
    List<EmailTemplate> findByUserIdAndIsDefaultTrue(String userId);
    List<EmailTemplate> findByIsDefaultTrue();
}
