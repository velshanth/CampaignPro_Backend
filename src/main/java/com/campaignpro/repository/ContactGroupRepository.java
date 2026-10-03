package com.campaignpro.repository;

import com.campaignpro.model.ContactGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactGroupRepository extends MongoRepository<ContactGroup, String> {
    List<ContactGroup> findByUserId(String userId);
    Optional<ContactGroup> findByUserIdAndName(String userId, String name);
    boolean existsByUserIdAndName(String userId, String name);
}
