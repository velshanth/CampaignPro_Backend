package com.campaignpro.controller;

import com.campaignpro.dto.ContactDTO;
import com.campaignpro.dto.ContactGroupDTO;
import com.campaignpro.model.User;
import com.campaignpro.repository.UserRepository;
import com.campaignpro.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserRepository userRepository;

    private String getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.campaignpro.exception.ResourceNotFoundException("User", "username", username));
        return user.getId();
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> getAllContacts(Authentication authentication) {
        String userId = getUserId(authentication);
        List<ContactDTO> contacts = contactService.getAllContacts(userId);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContactById(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        ContactDTO contact = contactService.getContactById(id, userId);
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO contactDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        ContactDTO createdContact = contactService.createContact(userId, contactDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(@PathVariable String id, @Valid @RequestBody ContactDTO contactDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        ContactDTO updatedContact = contactService.updateContact(id, userId, contactDTO);
        return ResponseEntity.ok(updatedContact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        contactService.deleteContact(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactDTO>> searchContacts(@RequestParam String searchTerm, Authentication authentication) {
        String userId = getUserId(authentication);
        List<ContactDTO> contacts = contactService.searchContacts(userId, searchTerm);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<ContactDTO>> getContactsByGroup(@PathVariable String groupName, Authentication authentication) {
        String userId = getUserId(authentication);
        List<ContactDTO> contacts = contactService.getContactsByGroup(userId, groupName);
        return ResponseEntity.ok(contacts);
    }

    // Contact Group Endpoints
    @GetMapping("/groups")
    public ResponseEntity<List<ContactGroupDTO>> getAllGroups(Authentication authentication) {
        String userId = getUserId(authentication);
        List<ContactGroupDTO> groups = contactService.getAllGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/groups")
    public ResponseEntity<ContactGroupDTO> createGroup(@Valid @RequestBody ContactGroupDTO groupDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        ContactGroupDTO createdGroup = contactService.createGroup(userId, groupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<ContactGroupDTO> updateGroup(@PathVariable String id, @Valid @RequestBody ContactGroupDTO groupDTO, Authentication authentication) {
        String userId = getUserId(authentication);
        ContactGroupDTO updatedGroup = contactService.updateGroup(id, userId, groupDTO);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id, Authentication authentication) {
        String userId = getUserId(authentication);
        contactService.deleteGroup(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contactId}/groups/{groupId}")
    public ResponseEntity<Void> addContactToGroup(@PathVariable String contactId, @PathVariable String groupId, Authentication authentication) {
        String userId = getUserId(authentication);
        contactService.addContactToGroup(groupId, contactId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{contactId}/groups/{groupId}")
    public ResponseEntity<Void> removeContactFromGroup(@PathVariable String contactId, @PathVariable String groupId, Authentication authentication) {
        String userId = getUserId(authentication);
        contactService.removeContactFromGroup(groupId, contactId, userId);
        return ResponseEntity.noContent().build();
    }
}
