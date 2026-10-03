package com.campaignpro.repository;

import com.campaignpro.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
    List<Contact> findByUserId(String userId);
    List<Contact> findByUserIdAndActiveTrue(String userId);
    List<Contact> findByUserIdAndGroupsContaining(String userId, String groupName);
    List<Contact> findByUserIdAndEmailContainingIgnoreCase(String userId, String email);
    List<Contact> findByUserIdAndFirstNameContainingIgnoreCase(String userId, String firstName);
    List<Contact> findByUserIdAndLastNameContainingIgnoreCase(String userId, String lastName);
    
    @Query("{'userId': ?0, '$or': [{'firstName': {$regex: ?1, $options: 'i'}}, {'lastName': {$regex: ?1, $options: 'i'}}, {'email': {$regex: ?1, $options: 'i'}}]}")
    List<Contact> searchContacts(String userId, String searchTerm);
    
    long countByUserId(String userId);
}
